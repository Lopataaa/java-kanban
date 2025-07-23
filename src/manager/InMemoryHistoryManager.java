package manager;

import task.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private HashMap<Integer, Task> historyMapId = new HashMap<>();
    private LinkedList<Task> historyList = new LinkedList<>();
    private Map<Integer, Node> nodeMap = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void add(Task task) {
        if (nodeMap.containsKey(task.getId())) { // проверка на наличие задачи в списке
            Node nodeToRemove = nodeMap.get(task.getId());
            removeNode(nodeToRemove); // удаление из списка
            nodeMap.remove(task.getId()); // удаление из HashMap
        }

        linkLast(task); // добавление задачи в конец списка
        nodeMap.put(task.getId(), tail); // После добавления задачи не забудьте обновить значение узла в HashMap
    }


    public void remove(int id) {
        Task task = getTaskId(id); // поиск задачи с определенным id
        if (task != null) {
            historyMapId.remove(id); // удаление заданной задачи из коллекций
            historyList.remove(task);
        }
    }

    public Task getTaskId(int id) {
        return historyMapId.get(id);
    }

    class Node {
        Task task;
        Node prev;
        Node next;

        public Node(Task task) {
            this.task = task;
            this.prev = null;
            this.next = null;
        }
    }

    public void linkLast(Task task) { // добавить задачу в конец списка
        Node newNode = new Node(task);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        nodeMap.put(task.getId(), newNode);
    }

    public List<Task> getTasks() { // собирать все задачи из списка в обычный ArrayList
        List<Task> tasks = new ArrayList<>();
        Node currentNode = head;
        while (currentNode != null) {
            tasks.add(currentNode.task);
            currentNode = currentNode.next;
        }
        return tasks;
    }

    public void removeNode(Node nodeToRemove) {
        if (nodeToRemove == null) {
            return;
        }

        if (nodeToRemove.prev != null) {
            nodeToRemove.prev.next = nodeToRemove.next;
        } else {
            head = nodeToRemove.next;
        }

        if (nodeToRemove.next != null) {
            nodeToRemove.next.prev = nodeToRemove.prev;
        } else {
            tail = nodeToRemove.prev;
        }
    }
}