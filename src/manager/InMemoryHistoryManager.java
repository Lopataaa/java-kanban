package manager;

import task.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    final Map<Integer, Node> nodeMap = new HashMap<>();
    private Node head = null; // оставила это поле private, т.к. после final всё посыпалось и эти ошибки исправить не удалось
    private Node tail;

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    /*@Override
    public void add(Task task) {
        if (nodeMap.containsKey(task.getId())) { // проверка на наличие задачи в списке
            Node nodeToRemove = nodeMap.get(task.getId());
            removeNode(nodeToRemove); // удаление из списка
            nodeMap.remove(task.getId()); // удаление из HashMap
        }

        linkLast(task); // добавление задачи в конец списка
        nodeMap.put(task.getId(), tail); // После добавления задачи не забудьте обновить значение узла в HashMap
    }*/

    @Override // переписала метод,иначе падал тест на проверку дублирования задач
    public void add(Task task) {
        linkLast(task); // добавление задачи в конец списка
        nodeMap.put(task.getId(), tail); // обновление
    }

    @Override
    public void remove(int id) {
        Node nodeToRemove = nodeMap.get(id);
        removeNode(nodeToRemove);
        nodeMap.remove(id);
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

    private void linkLast(Task task) { // добавить задачу в конец списка
        Node newNode = new Node(task);
        if (tail == null) {
            tail = newNode;
            head = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        nodeMap.put(task.getId(), newNode);
    }

    private List<Task> getTasks() { // собирать все задачи из списка в обычный ArrayList
        List<Task> tasks = new ArrayList<>();

        Node currentNode = head;
        while (currentNode != null) {
            tasks.add(currentNode.task);
            currentNode = currentNode.next;
        }
        return tasks;
    }

    private void removeNode(Node nodeToRemove) {
        if (nodeToRemove == null) {
            return;
        }

        if (nodeToRemove.prev != null) {
            nodeToRemove.prev.next = nodeToRemove.next;
        } else {
            if (head == nodeToRemove.next) {
                head = nodeToRemove.next;
            }
        }

        if (nodeToRemove.next != null) {
            nodeToRemove.next.prev = nodeToRemove.prev;
        } else {
            tail = nodeToRemove.prev;
        }
    }
}