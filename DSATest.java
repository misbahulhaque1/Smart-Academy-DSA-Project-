package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import dsas.*;
import model.Course;
import java.util.*;

 // JUnit tests for DSA classes

public class DSATest {

    @Test
    @DisplayName("Test SLinkedList basic operations")
    void testSLinkedList() {
        SLinkedList<Integer> list = new SLinkedList<>();

        // Test add
        list.add(10);
        list.add(20);
        list.add(30);
        assertEquals(3, list.size());

        // Test get
        assertEquals(10, list.get(0));
        assertEquals(20, list.get(1));
        assertEquals(30, list.get(2));

        // Test addAt
        list.addAt(1, 15);
        assertEquals(4, list.size());
        assertEquals(15, list.get(1));

        // Test remove
        int removed = list.remove(1);
        assertEquals(15, removed);
        assertEquals(3, list.size());

        // Test contains
        assertTrue(list.contains(20));
        assertFalse(list.contains(100));
    }

    @Test
    @DisplayName("Test StackDS operations")
    void testStackDS() {
        StackDS<String> stack = new StackDS<>();

        // Test push
        stack.push("First");
        stack.push("Second");
        stack.push("Third");
        assertEquals(3, stack.size());

        // Test peek
        assertEquals("Third", stack.peek());
        assertEquals(3, stack.size()); // Size unchanged

        // Test pop
        assertEquals("Third", stack.pop());
        assertEquals("Second", stack.pop());
        assertEquals(1, stack.size());

        // Test isEmpty
        assertFalse(stack.isEmpty());
        stack.pop();
        assertTrue(stack.isEmpty());
    }

    @Test
    @DisplayName("Test QueueDS operations")
    void testQueueDS() {
        QueueDS<String> queue = new QueueDS<>();

        // Test enqueue
        queue.enqueue("First");
        queue.enqueue("Second");
        queue.enqueue("Third");
        assertEquals(3, queue.size());

        // Test peek
        assertEquals("First", queue.peek());
        assertEquals(3, queue.size());

        // Test dequeue (FIFO)
        assertEquals("First", queue.dequeue());
        assertEquals("Second", queue.dequeue());
        assertEquals(1, queue.size());

        // Test isEmpty
        assertFalse(queue.isEmpty());
        queue.dequeue();
        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("Test BSTCourse operations")
    void testBSTCourse() {
        BSTCourse bst = new BSTCourse();

        // Create test courses
        Course c1 = new Course("Java Programming", "Learn Java", true, 49.99, 1);
        c1.setId(1);
        Course c2 = new Course("Data Structures", "Learn DSA", true, 59.99, 1);
        c2.setId(2);
        Course c3 = new Course("Web Development", "HTML CSS JS", false, 0.0, 1);
        c3.setId(3);

        // Test insert
        bst.insert(c1);
        bst.insert(c2);
        bst.insert(c3);
        assertEquals(3, bst.size());

        // Test search
        Course found = bst.search("Java Programming");
        assertNotNull(found);
        assertEquals("Java Programming", found.getTitle());

        // Test partial search
        List<Course> results = bst.searchByPartialTitle("java");
        assertEquals(1, results.size());

        // Test sorted retrieval
        List<Course> sorted = bst.getAllSorted();
        assertEquals(3, sorted.size());
    }

    @Test
    @DisplayName("Test GraphDS operations")
    void testGraphDS() {
        GraphDS graph = new GraphDS();

        // Add vertices and edges
        graph.addEdge(1, 2, 1.5);
        graph.addEdge(1, 3, 1.0);
        graph.addEdge(2, 4, 1.2);
        graph.addEdge(3, 4, 0.8);

        assertEquals(4, graph.getVertexCount());

        // Test neighbors
        List<GraphDS.Edge> neighbors = graph.getNeighbors(1);
        assertEquals(2, neighbors.size());

        // Test direct recommendations
        List<Integer> directRecs = graph.getDirectRecommendations(1);
        assertFalse(directRecs.isEmpty());

        // Test DFS recommendations
        List<Integer> recs = graph.getRecommendations(1, 2);
        assertFalse(recs.isEmpty());
    }

    @Test
    @DisplayName("Test DSAUtils sorting")
    void testDSAUtils() {
        // Create test courses
        List<Course> courses = new ArrayList<>();
        Course c1 = new Course("Course A", "Desc", true, 50.0, 1);
        c1.setEnrolledCount(10);
        Course c2 = new Course("Course B", "Desc", true, 30.0, 1);
        c2.setEnrolledCount(20);
        Course c3 = new Course("Course C", "Desc", true, 70.0, 1);
        c3.setEnrolledCount(5);

        courses.add(c1);
        courses.add(c2);
        courses.add(c3);

        // Test enrollment sort
        DSAUtils.sortCoursesByEnrollment(courses);
        assertEquals(20, courses.get(0).getEnrolledCount());
        assertEquals(10, courses.get(1).getEnrolledCount());
        assertEquals(5, courses.get(2).getEnrolledCount());

        // Test price sort
        DSAUtils.sortCoursesByPrice(courses);
        assertEquals(30.0, courses.get(0).getPrice());
        assertEquals(50.0, courses.get(1).getPrice());
        assertEquals(70.0, courses.get(2).getPrice());
    }
}
