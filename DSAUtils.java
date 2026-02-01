package dsas;

import java.util.List;
import model.Course;

public class DSAUtils {

    // bubble sort (by enrolled, desc)
    public static void sortCoursesByEnrollment(List<Course> courses) {
        int n = courses.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (courses.get(j).getEnrolledCount() < courses.get(j + 1).getEnrolledCount()) {
                    Course temp = courses.get(j);
                    courses.set(j, courses.get(j + 1));
                    courses.set(j + 1, temp);
                }
            }
        }
    }

    // quick sort (by price, asc)
    public static void sortCoursesByPrice(List<Course> courses) {
        if (courses.isEmpty()) return;
        quickSort(courses, 0, courses.size() - 1);
    }

    private static void quickSort(List<Course> courses, int low, int high) {
        if (low < high) {
            int pi = partition(courses, low, high);
            quickSort(courses, low, pi - 1);
            quickSort(courses, pi + 1, high);
        }
    }

    private static int partition(List<Course> courses, int low, int high) {
        double pivot = courses.get(high).getPrice();
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (courses.get(j).getPrice() <= pivot) {
                i++;
                Course temp = courses.get(i);
                courses.set(i, courses.get(j));
                courses.set(j, temp);
            }
        }

        Course temp = courses.get(i + 1);
        courses.set(i + 1, courses.get(high));
        courses.set(high, temp);

        return i + 1;
    }

    // -------------------------------
    // NEW: Insertion Sort (ascending)
    // -------------------------------
    public static <T extends Comparable<T>> void insertionSort(List<T> list) {
        for (int i = 1; i < list.size(); i++) {
            T key = list.get(i);
            int j = i - 1;

            while (j >= 0 && list.get(j).compareTo(key) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }

            list.set(j + 1, key);
        }
    }

    // -------------------------------
    // NEW: Selection Sort (ascending)
    // -------------------------------
    public static <T extends Comparable<T>> void selectionSort(List<T> list) {
        int n = list.size();

        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;

            for (int j = i + 1; j < n; j++) {
                if (list.get(j).compareTo(list.get(minIndex)) < 0) {
                    minIndex = j;
                }
            }

            // swap
            T temp = list.get(i);
            list.set(i, list.get(minIndex));
            list.set(minIndex, temp);
        }
    }
}
