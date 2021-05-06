
/*
1. Написать метод, которому в качестве аргумента передается не пустой одномерный целочисленный массив.
 Метод должен вернуть новый массив, который получен путем вытаскивания из исходного массива элементов,
 идущих после последней четверки. Входной массив должен содержать хотя бы одну четверку, иначе в методе
 необходимо выбросить RuntimeException. Написать набор тестов для этого метода (по 3-4 варианта входных
 данных). Вх: [ 1 2 4 4 2 3 4 1 7 ] -> вых: [ 1 7 ].
2. Написать метод, который проверяет состав массива из чисел 1 и 4. Если в нем нет хоть одной четверки
или единицы, то метод вернет false; Написать набор тестов для этого метода (по 3-4 варианта входных данных).
 */

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;


class HomeWorkTest {
    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowRuntimeExceptionWhenArrayIsEmpty(int[] values) {
        Assertions.assertThrows(RuntimeException.class, () -> HomeWork.findAfterFour(values));
    }

    @ParameterizedTest
    @MethodSource("notFoundFour")
    void shouldThrowRuntimeExceptionWhenFourNotFound(int expected, int a, int b) {
        Assertions.assertThrows(RuntimeException.class, () -> HomeWork.findAfterFour(new int[]{}));
    }

    private static Stream<Arguments> notFoundFour() {
        return Stream.of(
                Arguments.arguments(0, 2, 5),
                Arguments.arguments(0, 0, 3),
                Arguments.arguments(0, 6, 0),
                Arguments.arguments(1, 6, 4)
        );
    }

    @ParameterizedTest
    @MethodSource("returnAfterLastFour")
    void shouldReturnValuesAfterLastFour(int[] array, int[] values) {
        Assertions.assertArrayEquals(array, HomeWork.findAfterFour(values));
    }

    private static Stream<Arguments> returnAfterLastFour() {
        return Stream.of(
                Arguments.arguments(new int[]{5, 6, 7, 9}, new int[]{2, 3, 5, 4, 5, 6, 7, 9}),
                Arguments.arguments(new int[]{6, 7, 9}, new int[]{2, 3, 5, 4, 4, 6, 7, 9}),
                Arguments.arguments(new int[]{5, 3, 5, 6, 7, 9}, new int[]{2, 4, 5, 3, 5, 6, 7, 9})

        );
    }

    @RunWith(Parameterized.class)
    public static class CheckOneAndFourTest {

        @Parameterized.Parameters
        public static Collection<Object[]> data() {

             return  Arrays.asList(new Object[][]{
                     {new int[]{1, 4, 1, 1}, true},
                     {new int[]{4, 4, 4, 4}, false},
                     {new int[]{4, 4, 4, 1, 4}, true},
                     {new int[]{1, 3, 4, 1}, false}
             });
         }
            private int[] arr;
            private boolean result;
            public CheckOneAndFourTest(int[] arr, boolean result){
                this.arr=arr;
                this.result=result;
         }
         @Test
        public void testOneAndFour(){
                Assert.assertEquals(result,HomeWork.checkOneAndFour(arr));
         }
    }

}
