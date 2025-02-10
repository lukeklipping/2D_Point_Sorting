package src.edu.iastate.cs2280.hw3;

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import edu.iastate.cs2280.hw2.Point;

/**
 * Implementation of the list interface based on linked nodes that store
 * multiple items per node. Rules for adding and removing elements ensure that
 * each node (except possibly the last one) is at least half full.
 * 
 * @author lukeklipping
 */
public class StoutList<E extends Comparable<? super E>> extends AbstractSequentialList<E> {
	/**
	 * Default number of elements that may be stored in each node.
	 */
	private static final int DEFAULT_NODESIZE = 4;

	/**
	 * Number of elements that can be stored in each node.
	 */
	private final int nodeSize;

	/**
	 * Dummy node for head. It should be private but set to public here only for
	 * grading purpose. In practice, you should always make the head of a linked
	 * list a private instance variable.
	 */
	public Node head;

	/**
	 * Dummy node for tail.
	 */
	private Node tail;

	/**
	 * Number of elements in the list.
	 */
	private int size;

	/**
	 * Constructs an empty list with the default node size.
	 */
	public StoutList() {
		this(DEFAULT_NODESIZE);
	}

	/**
	 * Constructs an empty list with the given node size.
	 * 
	 * @param nodeSize number of elements that may be stored in each node, must be
	 *                 an even number
	 */
	public StoutList(int nodeSize) {
		if (nodeSize <= 0 || nodeSize % 2 != 0)
			throw new IllegalArgumentException();

		// dummy nodes
		head = new Node();
		tail = new Node();
		head.next = tail;
		tail.previous = head;
		this.nodeSize = nodeSize;
	}

	/**
	 * Constructor for grading only. Fully implemented.
	 * 
	 * @param head
	 * @param tail
	 * @param nodeSize
	 * @param size
	 */
	public StoutList(Node head, Node tail, int nodeSize, int size) {
		this.head = head;
		this.tail = tail;
		this.nodeSize = nodeSize;
		this.size = size;
	}

	/**
	 * Returns size of StoutList
	 */
	@Override
	public int size() {

		return size;
	}

	/**
	 * Adds item to the end of list based on prescribed rules
	 */
	@Override
	public boolean add(E item) {

		if (item == null) {
			throw new NullPointerException();
		}
		// if size is 0, create new node
		if (size == 0) {
			Node newNode = new Node();
			newNode.addItem(item);
			link(head, newNode, tail);

		} else {
			// if node is not full, add before dummy tail
			if (tail.previous.count < nodeSize) {
				tail.previous.addItem(item);
			} else {
				// node is full, create new Node
				Node newNode = new Node();
				newNode.addItem(item);
				link(tail.previous, newNode, tail);

			}
		}
		size++;
		return true;
	}

	/**
	 * Adds item to specific position in list, merging any nodes needed
	 */
	@Override
	public void add(int pos, E item) {

		// if list is empty, add to new node
		if (size == 0 && pos == 0) {
			// call base add()
			add(item);
			return;
		}
		if (pos < 0 || pos > size) {
			throw new IndexOutOfBoundsException();
		}
		if (item == null) {
			// check for null item
			throw new NullPointerException();
		}

		NodeInfo info = new NodeInfo(head.next, 0);
		NodeInfo nodeInfo = info.find(pos);
		Node node = nodeInfo.node;
		int offset = nodeInfo.offset;

		if (node.count < nodeSize ) {
			if(offset < node.count) {
				node.addItem(offset, item);

			} else {
				node.addItem(item);

			}
		} else {
			// node is full, split node
			Node newTemp = new Node();
			int half = nodeSize / 2;

			// Move half of the items to the new node
			for (int i = half; i < nodeSize; i++) {
				newTemp.addItem(node.data[i]);
			}

			// Remove moved items from the original node
			for (int i = nodeSize - 1; i >= half; i--) {
				node.removeItem(i);
			}

			link(node, newTemp, node.next);

			if (offset <= half) {
				node.addItem(offset, item);
			} else {
				newTemp.addItem(offset - half, item);
			}

		}
		size++;
	}

	/**
	 * Removes a node at a position, merging any needed nodes
	 */
	@Override
	public E remove(int pos) {
		if (pos < 0 || pos >= size) {
			throw new IndexOutOfBoundsException();
		}

		// find new info
		NodeInfo info = new NodeInfo(head.next, pos).find(pos);
		Node currentNode = info.node;
		int offset = info.offset;
		int half = nodeSize / 2;
		E nodeValue = currentNode.data[offset];

		// current node has 1 element and next node is dummy tail
		// delete node
		if (currentNode.count == 1 && currentNode.next == tail) {
			currentNode.removeItem(offset);
			unlink(currentNode);
		}
		// currentNode next is tail, but has more than one element
		else if (currentNode.next == tail || currentNode.count > half) {
			currentNode.removeItem(offset);
		} else {
			currentNode.removeItem(offset);
			Node nextNode = currentNode.next;

			if (nextNode.count > half) {
				// shift node data from successor
				currentNode.addItem(nextNode.data[0]);
				nextNode.removeItem(0);
			} else {

				for (int i = 0; i < nextNode.count; i++) {
					currentNode.addItem(nextNode.data[i]);
				}
				unlink(nextNode);

			}
		}
		size--;
		return nodeValue;
	}

	/**
	 * Sort all elements in the stout list in the NON-DECREASING order. You may do
	 * the following. Traverse the list and copy its elements into an array,
	 * deleting every visited node along the way. Then, sort the array by calling
	 * the insertionSort() method. (Note that sorting efficiency is not a concern
	 * for this project.) Finally, copy all elements from the array back to the
	 * stout list, creating new nodes for storage. After sorting, all nodes but
	 * (possibly) the last one must be full of elements.
	 * 
	 * Comparator<E> must have been implemented for calling insertionSort().
	 */
	public void sort() {
		E dataArray[] = (E[]) new Comparable[size];

		// move linkedList to array
		dataArray = linkedToArray(dataArray, head);

		insertionSort(dataArray, new SortComparator());

		// move back to linkedList
		for (int i = 0; i <= dataArray.length - 1; i++) {
			add(dataArray[i]);
		}
	}

	/**
	 * Sort all elements in the stout list in the NON-INCREASING order. Call the
	 * bubbleSort() method. After sorting, all but (possibly) the last nodes must be
	 * filled with elements.
	 * 
	 * Comparable<? super E> must be implemented for calling bubbleSort().
	 */
	public void sortReverse() {
		E dataArray[] = (E[]) new Comparable[size];

		// move linkedList to array
		dataArray = linkedToArray(dataArray, head);

		bubbleSort(dataArray);

		// move back to linkedList
		for (int i = 0; i < dataArray.length; i++) {
			add(dataArray[i]);
		}
	}

	/**
	 * Creates simple StoutIterator
	 */
	@Override
	public Iterator<E> iterator() {
		return new StoutIterator();
	}

	/**
	 * Creates StoutListIterator
	 */
	@Override
	public ListIterator<E> listIterator() {
		return new StoutListIterator();
	}

	/**
	 * Creates StoutList Iterator at index
	 */
	@Override
	public ListIterator<E> listIterator(int index) {
		return new StoutListIterator(index);
	}

	/**
	 * Returns a string representation of this list showing the internal structure
	 * of the nodes.
	 */
	public String toStringInternal() {
		return toStringInternal(null);
	}

	/**
	 * Returns a string representation of this list showing the internal structure
	 * of the nodes and the position of the iterator.
	 *
	 * @param iter an iterator for this list
	 */
	public String toStringInternal(ListIterator<E> iter) {
		int count = 0;
		int position = -1;
		if (iter != null) {
			position = iter.nextIndex();
		}

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		Node current = head.next;
		while (current != tail) {
			sb.append('(');
			E data = current.data[0];
			if (data == null) {
				sb.append("-");
			} else {
				if (position == count) {
					sb.append("| ");
					position = -1;
				}
				sb.append(data.toString());
				++count;
			}

			for (int i = 1; i < nodeSize; ++i) {
				sb.append(", ");
				data = current.data[i];
				if (data == null) {
					sb.append("-");
				} else {
					if (position == count) {
						sb.append("| ");
						position = -1;
					}
					sb.append(data.toString());
					++count;

					// iterator at end
					if (position == size && count == size) {
						sb.append(" |");
						position = -1;
					}
				}
			}
			sb.append(')');
			current = current.next;
			if (current != tail)
				sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Helper method to link a new node between two existing nodes.
	 * 
	 * @param prevNode
	 * @param newNode
	 * @param nextNode
	 */
	private void link(Node prevNode, Node newNode, Node nextNode) {
		if (prevNode == null || newNode == null || nextNode == null) {
			throw new IllegalArgumentException();
		}

		// links nodes together
		prevNode.next = newNode;
		newNode.previous = prevNode;
		newNode.next = nextNode;
		nextNode.previous = newNode;
	}

	/**
	 * Helper method to unlink a target node
	 * 
	 * @param targetNode
	 */
	private void unlink(Node targetNode) {
		if (targetNode == null || targetNode.next == null || targetNode.previous == null) {
			throw new NoSuchElementException();
		}
		// unlinks node
		targetNode.previous.next = targetNode.next;
		targetNode.next.previous = targetNode.previous;
		targetNode = null;
	}

	/**
	 * Helper method to shift a linkedList to an array for sorting
	 * 
	 * @param arr
	 * @param target
	 * @return
	 */
	private E[] linkedToArray(E arr[], Node target) {
		int dataSize = 0;
		Node currNode = head.next;
		while (currNode != tail) {
			for (int i = 0; i < currNode.count; i++) {
				// check for null elements
				if (currNode.data[i] != null) {
					arr[dataSize] = currNode.data[i];
				}
				dataSize++;

			}
			currNode = currNode.next;
		}
		// clears linked list
		clear();
		return arr;
	}

	/**
	 * Node type for this list. Each node holds a maximum of nodeSize elements in an
	 * array. Empty slots are null.
	 */
	private class Node {
		/**
		 * Array of actual data elements.
		 */
		// Unchecked warning unavoidable.
		public E[] data = (E[]) new Comparable[nodeSize];

		/**
		 * Link to next node.
		 */
		public Node next;

		/**
		 * Link to previous node;
		 */
		public Node previous;

		/**
		 * Index of the next available offset in this node, also equal to the number of
		 * elements in this node.
		 */
		public int count;

		/**
		 * Adds an item to this node at the first available offset. Precondition: count
		 * < nodeSize
		 * 
		 * @param item element to be added
		 */
		void addItem(E item) {
			if (count >= nodeSize) {
				return;
			}
			data[count++] = item;
			// useful for debugging
			// System.out.println("Added " + item.toString() + " at index " + count + " to
			// node " + Arrays.toString(data));
		}

		/**
		 * Adds an item to this node at the indicated offset, shifting elements to the
		 * right as necessary.
		 * 
		 * Precondition: count < nodeSize
		 * 
		 * @param offset array index at which to put the new element
		 * @param item   element to be added
		 */
		void addItem(int offset, E item) {
			if (count >= nodeSize) {
				return;
			}
			for (int i = count - 1; i >= offset; --i) {
				data[i + 1] = data[i];
			}
			++count;
			data[offset] = item;
			// useful for debugging
//      System.out.println("Added " + item.toString() + " at index " + offset + " to node: "  + Arrays.toString(data));
		}

		/**
		 * Deletes an element from this node at the indicated offset, shifting elements
		 * left as necessary. Precondition: 0 <= offset < count
		 * 
		 * @param offset
		 */
		void removeItem(int offset) {
			E item = data[offset];
			for (int i = offset + 1; i < nodeSize; ++i) {
				data[i - 1] = data[i];
			}
			data[count - 1] = null;
			--count;
		}
	}

	/**
	 * Node info class for providing node variables
	 */
	private class NodeInfo {
		/**
		 * Node object
		 */
		public Node node;
		/**
		 * Offset within node
		 */
		public int offset;

		/**
		 * Constructor setting node and offset
		 * 
		 * @param node
		 * @param offset
		 */
		public NodeInfo(Node node, int offset) {
			this.node = node;
			this.
			offset = offset;
		}

		/**
		 * Finds element based on current position and offset
		 * 
		 * @param pos
		 * @return
		 */
		NodeInfo find(int pos) {
			if (pos < 0 || pos > size()) {
				throw new IndexOutOfBoundsException();
			}

			Node currentNode = head.next;
			int offset = 0;

			while (currentNode != tail) {

				if (offset + currentNode.count > pos) {
					// once is found, return nodeInfo object					
					return new NodeInfo(currentNode, pos - offset);
				}
				offset += currentNode.count;
				if(offset == size()) {
					return new NodeInfo(currentNode, offset);
				}				
				currentNode = currentNode.next;
				
			}

			throw new IndexOutOfBoundsException();
		}

	}

	/**
	 * StoutListIterator with StoutList rules implements ListIterator<E>
	 */
	private class StoutListIterator implements ListIterator<E> {

		// instance variables ...
		private Node cursor;

		/**
		 * Global logical index variable for within nodes
		 */
		private int index;
		/**
		 * Direction variable for basing rules
		 */
		private int direction;
		/**
		 * Internal node index count
		 */
		private int nodeIndex;
		/**
		 * Direction constant for previous
		 */
		private static final int BEHIND = -1;
		/**
		 * Zero direction
		 */
		private static final int NONE = 0;
		/**
		 * Direction constant for next
		 */
		private static final int AHEAD = 1;

		/**
		 * Default constructor
		 */
		public StoutListIterator() {

			this(0);

		}

		/**
		 * Constructor finds node at a given position
		 * 
		 * @param pos
		 */
		public StoutListIterator(int pos) {
			if (pos < 0 || pos > size()) {
				throw new IndexOutOfBoundsException();
			}

			index = pos;
			nodeIndex = 0;
			direction = NONE;
			NodeInfo nodeInfo = new NodeInfo(head.next, 0);
			cursor = nodeInfo.find(pos).node;

		}

		/**
		 * Checks if there is a next element
		 */
		@Override
		public boolean hasNext() {
			return index < size();
		}

		/**
		 * Checks if there is a previous element
		 */
		@Override
		public boolean hasPrevious() {
			return index > 0;
		}

		/**
		 * Shifts index to next element, and shifts nodes based on size
		 */
		@Override
		public E next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			E element = cursor.data[nodeIndex];

			nodeIndex++;

			if (cursor.count == nodeIndex && cursor.next != tail) {
				cursor = cursor.next;
				nodeIndex = 0;
			}

			index++;
			direction = BEHIND;
			return element;
		}

		/**
		 * Shifts index to previous element, and shifts nodes based on size
		 */
		@Override
		public E previous() {
			if (!hasPrevious()) {
				throw new IllegalStateException();
			}

			nodeIndex--;
			if (nodeIndex < 0) {
				cursor = cursor.previous;
				nodeIndex = cursor.count - 1; // Set nodeIndex to the last element of the previous node
			}

			E element = cursor.data[nodeIndex];

			index--;
			direction = AHEAD;

			return element;
		}

		/**
		 * Removes element at current index if direction is applied. Shifts nodes if
		 * needed
		 */
		@Override
		public void remove() {
			if (direction == NONE) {
				throw new IllegalStateException();
			}

			if (direction == BEHIND) {
				StoutList.this.remove(index - 1);
				// Adjust index to reflect the new size after removal
			} else if (direction == AHEAD) {
				StoutList.this.remove(index);
			}
			index--;

			// Reset direction and adjust node index
			direction = NONE;
			if (nodeIndex > 0) {
				nodeIndex--;
			} else {
				// Move to the previous node if nodeIndex reaches 0
				if (cursor.previous != null && cursor.previous != head) {
					cursor = cursor.previous;
					nodeIndex = cursor.count - 1;
				} else {
					nodeIndex = 0;
				}
			}

		}

		/**
		 * Returns nextIndex
		 */
		@Override
		public int nextIndex() {
			return index;

		}

		/**
		 * Returns previous index
		 */
		@Override
		public int previousIndex() {
			return index - 1;

		}

		/**
		 * Sets element based on logical direction
		 */
		@Override
		public void set(E e) {
			if (direction == NONE) {
				throw new IllegalStateException();
			} else if (direction == BEHIND) {
				if (nodeIndex >= 0 && nodeIndex < cursor.data.length) {
					cursor.data[nodeIndex - 1] = e;
				}
			} else {
				if (nodeIndex + 1 < cursor.data.length) {
					cursor.data[nodeIndex + 1] = e;
				}
			}

		}

		/**
		 * Adds element to the left of index, shifting other elements. Sets direction =
		 * NONE
		 */
		@Override
		public void add(E e) {
			if (e == null) {
				throw new NullPointerException();
			}
			if (direction == NONE) {
				throw new IllegalStateException();
			}

			StoutList.this.add(nodeIndex, e);
			direction = NONE;
		}

	}

	/**
	 * Simple StoutIterator
	 */
	private class StoutIterator implements Iterator<E> {
		private int currIndex;
		private Node currNode;

		public StoutIterator() {
			this.currIndex = 0;
			this.currNode = head.next;
		}

		@Override
		public boolean hasNext() {
			return currIndex < size && currIndex < currNode.count;
		}

		@Override
		public E next() {
			if (!hasNext() || currNode.count == 0) {
				throw new NoSuchElementException();
			}
			E data = currNode.data[currIndex];
			currIndex++;

			if (currIndex >= currNode.count) {
				currNode = currNode.next;
				currIndex = 0;
			}

			return data;
		}

	}

	/**
	 * Sort an array arr[] using the insertion sort algorithm in the NON-DECREASING
	 * order.
	 * 
	 * @param arr  array storing elements from the list
	 * @param comp comparator used in sorting
	 */
	private void insertionSort(E[] arr, Comparator<? super E> comp) {
		int n = arr.length;
		for (int i = 1; i < n; i++) {
			E key = arr[i];
			int j = i - 1;
			while (j >= 0 && comp.compare(arr[j], key) > 0) {
				arr[j + 1] = arr[j];
				j--;
			}
			arr[j + 1] = key;
		}

	}

	/**
	 * Sort arr[] using the bubble sort algorithm in the NON-INCREASING order. For a
	 * description of bubble sort please refer to Section 6.1 in the project
	 * description. You must use the compareTo() method from an implementation of
	 * the Comparable interface by the class E or ? super E.
	 * 
	 * @param arr array holding elements from the list
	 */
	private void bubbleSort(E[] arr) {
		E temp;

		for (int i = arr.length - 1; i > 0; i--) {
			for (int j = 0; j < i; j++) {
				if (arr[j].compareTo(arr[j + 1]) < 0) { // <
					temp = arr[j];
					arr[j] = arr[j + 1];
					arr[j + 1] = temp;
				}
			}
		}
	}

	/**
	 * Implementation of Comparable Interface
	 * 
	 * @author lukeklipping
	 * 
	 * @param <E>
	 */
	public class SortComparator<E extends Comparable<E>> implements Comparator<E> {

		@Override
		public int compare(E a, E b) {
			return a.compareTo(b);
		}

	}

}