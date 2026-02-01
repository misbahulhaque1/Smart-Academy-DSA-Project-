package dsas;

import java.util.ArrayList;
import java.util.List;
import model.Course;

public class BSTCourse {

    public static class TreeNode {
        Course course;
        TreeNode left;
        TreeNode right;

        public TreeNode(Course course) {
            this.course = course;
            this.left = null;
            this.right = null;
        }
    }

    private TreeNode root;
    private int size;

    public BSTCourse() {
        this.root = null;
        this.size = 0;
    }

    // insert
    public void insert(Course course) {
        root = insertRec(root, course);
        size++;
    }

    private TreeNode insertRec(TreeNode node, Course course) {
        if (node == null) {
            return new TreeNode(course);
        }
        int comparison = course.getTitle().compareToIgnoreCase(node.course.getTitle());
        if (comparison < 0) {
            node.left = insertRec(node.left, course);
        } else if (comparison > 0) {
            node.right = insertRec(node.right, course);
        }
        // equal -> skip
        return node;
    }

    // search exact title
    public Course search(String title) {
        return searchRec(root, title);
    }

    private Course searchRec(TreeNode node, String title) {
        if (node == null) return null;
        int comparison = title.compareToIgnoreCase(node.course.getTitle());
        if (comparison == 0) {
            return node.course;
        } else if (comparison < 0) {
            return searchRec(node.left, title);
        } else {
            return searchRec(node.right, title);
        }
    }

    // search contains
    public List<Course> searchByPartialTitle(String partialTitle) {
        List<Course> results = new ArrayList<>();
        searchByPartialTitleRec(root, partialTitle.toLowerCase(), results);
        return results;
    }

    private void searchByPartialTitleRec(TreeNode node, String partialTitle, List<Course> results) {
        if (node == null) return;
        if (node.course.getTitle().toLowerCase().contains(partialTitle)) {
            results.add(node.course);
        }
        searchByPartialTitleRec(node.left, partialTitle, results);
        searchByPartialTitleRec(node.right, partialTitle, results);
    }

    // inorder list
    public List<Course> getAllSorted() {
        List<Course> courses = new ArrayList<>();
        inOrderTraversal(root, courses);
        return courses;
    }

    private void inOrderTraversal(TreeNode node, List<Course> courses) {
        if (node != null) {
            inOrderTraversal(node.left, courses);
            courses.add(node.course);
            inOrderTraversal(node.right, courses);
        }
    }

    // size
    public int size() {
        return size;
    }

    // empty?
    public boolean isEmpty() {
        return root == null;
    }

    // clear
    public void clear() {
        root = null;
        size = 0;
    }
}
