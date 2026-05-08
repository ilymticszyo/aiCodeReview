package io.binghe.ai.review.test;

public class test {
    public static void main(String[] args) {
        int[] numbers = {64, 34, 25, 12, 22, 11, 90};

        System.out.println("Before sort:");
        printArray(numbers);

        bubbleSort(numbers);

        System.out.println("After sort:");
        printArray(numbers);
    }

    private static void bubbleSort(int[] numbers) {
        if (numbers == null || numbers.length < 2) {
            return;
        }

        int unsortedBoundary = numbers.length - 1;
        while (unsortedBoundary > 0) {
            int lastSwapIndex = 0;
            for (int j = 0; j < unsortedBoundary; j++) {
                if (numbers[j] > numbers[j + 1]) {
                    swap(numbers, j, j + 1);
                    lastSwapIndex = j;
                }
            }

            if (lastSwapIndex == 0) {
                break;
            }
            unsortedBoundary = lastSwapIndex;
        }
    }

    private static void printArray(int[] numbers) {
        if (numbers == null) {
            System.out.println("null");
            return;
        }

        for (int number : numbers) {
            System.out.print(number + " ");
        }
        System.out.println();
    }

    private static void swap(int[] numbers, int leftIndex, int rightIndex) {
        int temp = numbers[leftIndex];
        numbers[leftIndex] = numbers[rightIndex];
        numbers[rightIndex] = temp;
    }
}
