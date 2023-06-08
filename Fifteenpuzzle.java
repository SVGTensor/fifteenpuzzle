// Реализация поиска решения в игре 15

import java.util.*; // Импортируем библиотеки
import java.lang.*; // Импортируем Integer
import java.math.*; // Импортируем abs, random

/**
	@* author: Shishliakov Vladimir
	@* project: Fifteen puzzle
*/

// Класс игрового поля
class Board {
	public int[][] blocks; // Массив, означающий игровое поле (пустое место - нуль)
	public int zeroX; // Коррдината по Ox пустой клетки
	public int zeroY; // Координата по Oy пустой клетки
	public int metricks; // Оптимальность данного расположнения фишек

	public Board(int size) { // Конструктор пустого поля
		blocks = new int[size][size];
		zeroX = 0;
		zeroY = 0;
	}
	
	public Board(int[][] blocks) {
		int[][] blocks2 = deepCopy(blocks); // Копируем массив (не ссылка)
		this.blocks = blocks2; // Сохраняем копию внутри класса

		for (int i = 0; i < blocks.length; i++) {  //  в этом цикле определяем координаты нуля и вычисляем h(x)
			for (int j = 0; j < blocks[i].length; j++) {
				if (blocks[i][j] == 0) { // Высчитываем, где у нас пустая клетка
					zeroX = i;
					zeroY = j;
				}
			}
		}
	}
	
	public void copyBoard(Board board) {
		int[][] blocks2 = deepCopy(board.blocks); // Копируем массив (не ссылка)
		this.blocks = blocks2; // Сохраняем копию внутри класса
		
		for (int i = 0; i < blocks.length; i++) {  //  в этом цикле определяем координаты нуля и вычисляем h(x)
			for (int j = 0; j < blocks[i].length; j++) {
				if (blocks[i][j] == 0) { // Высчитываем, где у нас пустая клетка
					zeroX = i;
					zeroY = j;
				}
			}
		}
	}
	
	public void init() { // Инициализируем начальное поле (правильное расположение фишек)
		int k=1;
		for(int i=0; i<blocks.length; i++) {
			for(int j=0; j<blocks.length; j++) {
				if(k<blocks.length*blocks.length)
					blocks[i][j] = k;
				else
					blocks[i][j] = 0;
				k++;
			}
		}
		zeroX = blocks.length-1;
		zeroY = blocks.length-1;
	}
	
	public void mix(int n) { // Перемешать фишки случайным образом n раз (предполагается, что поле уже инициализировано)
		for(int i=0; i<n; i++) {
			ArrayList<Board> nbrs = neighbors();
			int index = (int)(Math.random()*(nbrs.size()));
			Object obj = nbrs.get(index);
			Board board = (Board)obj;
			copyBoard(board);
		}
	}
	
	public int L1() { // Суммарное Манхеттеновское расстояние по всем фишкам
		int l1 = 0;
		for (int i = 0; i < blocks.length; i++) {
			for (int j = 0; j < blocks[i].length; j++) {
				int optimalX = blocks[blocks.length-1].length-1;
				int optimalY = blocks.length-1;
				if (blocks[i][j] != 0) {
					optimalX = (blocks[i][j]-1) % blocks[i].length;
					optimalY = (blocks[i][j]-1) / blocks[i].length;
				}
				l1+=(Math.abs(optimalX-j)+Math.abs(optimalY-i));
			}
		}
		return l1;
	}

	public int getMetricks() {
		return L1();
	}

	public boolean isGoal() { // Очевидно, что если суммарное Манхеттенское расстояние равно нулю, то все фишки имеют оптимальные позиции
		return L1() == 0;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Board board = (Board) o;

		if (board.blocks.length != this.blocks.length) return false;
		for (int i = 0; i < blocks.length; i++) {
			for (int j = 0; j < blocks[i].length; j++) {
				if (blocks[i][j] != board.blocks[i][j]) {
					return false;
				}
			}
		}

		return true;
	}

	public ArrayList<Board> neighbors() {  // Все возможные ходы из данной позиции
		ArrayList<Board> boardList = new ArrayList<Board>();
		
		Board position = change(deepCopy(blocks), zeroX, zeroY, zeroX, zeroY + 1);
		if(position!=null)
			boardList.add(position);
		
		position = change(deepCopy(blocks), zeroX, zeroY, zeroX, zeroY - 1);
		if(position!=null)
			boardList.add(position);
		
		position = change(deepCopy(blocks), zeroX, zeroY, zeroX-1, zeroY);
		if(position!=null)
			boardList.add(position);
		
		position = change(deepCopy(blocks), zeroX, zeroY, zeroX+1, zeroY);
		if(position!=null)
			boardList.add(position);

		return boardList;
	}

	private Board change(int[][] blocks2, int x1, int y1, int x2, int y2) { // Делаем ход

		if (x2 > -1 && x2 < blocks.length && y2 > -1 && y2 < blocks.length) { // Если ход допустим
			int t = blocks2[x2][y2];
			blocks2[x2][y2] = blocks2[x1][y1];
			blocks2[x1][y1] = t;
			return new Board(blocks2);
		} else
			return null;

	}

	private static int[][] deepCopy(int[][] original) {
		if (original == null) {
			return null;
		}

		int[][] result = new int[original.length][];
		for (int i = 0; i < original.length; i++) {
			result[i] = new int[original[i].length];
			for (int j = 0; j < original[i].length; j++) {
				result[i][j] = original[i][j];
			}
		}
		return result;
	}
	
	public void print() {
		for(int i=0; i<blocks.length; i++) {
			for(int j=0; j<blocks[i].length; j++) {
				System.out.print(blocks[i][j]+"\t");
			}
			System.out.println();
		}
	}
}

class Solver { // По сути, наш алгоритм Дейкстры с модификациями

	private Board initial; // Начальное состояние
	private List<Board> result = new ArrayList<Board>(); // Цепочка ходов, приводящих к решению задачи
	
	private class ITEM implements Comparable<ITEM> {	// Вспомогательный класс для хранения вершин графа
		private ITEM prevBoard;  // ссылка на предыдущий
		private Board board;   // сама позиция
		private int prevDist; // Количество ходов до этой вершины
		private int postDist; // Манхеттенское расстояние по всем фишкам (сколько еще примерно ходов нам надо сделать)

		public ITEM(ITEM prevBoard, Board board) { // Инициализируется вершина через ссылку на предыдущую вершину
													// и информацией о текущей вершине
			this.prevBoard = prevBoard;
			this.board = board;
			
			if(prevBoard==null)
				prevDist = 0;
			else
				prevDist = prevBoard.prevDist+1;
			
			postDist = board.L1();
			
		}

		public Board getBoard() { // Возвращает доску (позицию, хранящуюся в данной вершине графа)
			return board;
		}
		
		public int getDist() {
			return prevDist+postDist;
		}
		
		public int compareTo(ITEM it) { // Наследуем интерфейс Comparable
			return (new Integer(getDist()).compareTo(new Integer(it.getDist())));
		}
		
	}

	public Solver(Board initial) { // Конструктор solver-а
		this.initial = initial; // Сохраняем начальное расположение фишек

		// Собственно, дальше идет модифицированный алгоритм Дейкстры обхода части графа
		
		// Создаем очередь с приоритетом (пустую)
		// Приоритет задается "естественным" порядком класс ITEM (т.к. тот наследуется от Comparable-интерфейса)
		PriorityQueue<ITEM> priorityQueue = new PriorityQueue<ITEM>();
		
		// Добавляем в очередь начальную вершину графа (ссылка на предыдущую вершину равна null)
		priorityQueue.add(new ITEM(null, initial));
		
		while (true){
			ITEM board = priorityQueue.poll(); //  Вытаскиваем из очереди вершину с наименьшей мерой (удаляя ее)

			// Если данная вершина - это решение, то дальше цикл поиска крутить бесполезно
			if(board.board.isGoal()) {
				itemToList(new ITEM(board, board.board)); // Сохраняем список ходов
				return;
			}

			// В противном случае (если вершина - не является решением)
			Iterator<Board> iterator = board.board.neighbors().iterator(); // Получаем всех ее соседей (все вершины,
																	// соединенные с данной в графе)
			while(iterator.hasNext()) { // Проходимся по всем соседям (они изначально у нас в виде списка)
				Board board1 = iterator.next(); // Получаем очередного соседа
				
				// Если соседа не существует в пути - добавляем его в путь
				if(!containsInPath(board, board1))
					priorityQueue.add(new ITEM(board, board1));
			}

		}
	}

	// Получение пути решений (раскручиваем все ссылки типа prevBoard от текущей вершины до тех пор, пока не
	// упремся в null)
	private void itemToList(ITEM item){
		ITEM item2 = item;
		while (true){
			item2 = item2.prevBoard;
			if(item2 == null) {
				Collections.reverse(result);
				return;
			}
			result.add(item2.board);
		}
	}

	// Была ли уже такая позиция в пути
	private boolean containsInPath(ITEM item, Board board){
	  ITEM item2 = item;
	   while (true){
		   if(item2.board.equals(board)) return true;
		   item2 = item2.prevBoard;
		   if(item2 == null) return false;
	   }
	}

	// Возвращаем список ходов
	public Iterable<Board> getSolution() {
		return result;
	}


}

public class Fifteenpuzzle {
	public static void main(String[] args) {
		Board board = new Board(4); // Создаем новое игровое поле
		board.init(); // Инициализируем его (начальным положением фишек)
		System.out.println(board.isGoal());
		board.mix(50); // Запутываем его
		System.out.println(board.isGoal());
		board.print(); // Выводим на экран ту комбинацию фишек, которые мы будем давать на вход solver-у
		Solver solver = new Solver(board); // Создаем solver-а и ищем решение
		System.out.println("Solution:");
		for (Board i : solver.getSolution()) { // Выводим решение
				i.print();
				System.out.println();
		}
	}
}