package ToolKit;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenhao on 3/31/16.
 */
public class Combination {

    public static void main(String[] args) {
        int[] num = new int[200];
        for (int i = 0; i < num.length; i++) {
            num[i] = i;
        }
        print(combine(num, 2));
    }

    public static int[] bitprint(int u) {
        int[] x = new int[2];
        int i = 0;
        for (int n = 0; u > 0; ++n, u >>= 1)
            if ((u & 1) > 0)
                x[i++] = n;
        return x;
    }

    public static int bitcount(int u) {
        int n;
        for (n = 0; u > 0; ++n, u &= (u - 1)) ;//Turn the last set bit to a 0
        return n;
    }

    public static LinkedList<int[]> comb(int c, int n) {
        LinkedList<int[]> s = new LinkedList<>();
        for (int u = 0; u < 1 << n; u++)
            if (bitcount(u) == c)
                s.push(bitprint(u));
        return s;
    }

    public static List combine(int[] a, int m) {
        int n = a.length;

        List result = new ArrayList();

        int[] bs = new int[n];
        for (int i = 0; i < n; i++) {
            bs[i] = 0;
        }
        //初始化
        for (int i = 0; i < m; i++) {
            bs[i] = 1;
        }
        boolean flag = true;
        boolean tempFlag = false;
        int pos = 0;
        int sum = 0;
        //首先找到第一个10组合，然后变成01，同时将左边所有的1移动到数组的最左边
        do {
            sum = 0;
            pos = 0;
            tempFlag = true;
            result.add(print(bs, a, m));

            for (int i = 0; i < n - 1; i++) {
                if (bs[i] == 1 && bs[i + 1] == 0) {
                    bs[i] = 0;
                    bs[i + 1] = 1;
                    pos = i;
                    break;
                }
            }
            //将左边的1全部移动到数组的最左边

            for (int i = 0; i < pos; i++) {
                if (bs[i] == 1) {
                    sum++;
                }
            }
            for (int i = 0; i < pos; i++) {
                if (i < sum) {
                    bs[i] = 1;
                } else {
                    bs[i] = 0;
                }
            }

            //检查是否所有的1都移动到了最右边
            for (int i = n - m; i < n; i++) {
                if (bs[i] == 0) {
                    tempFlag = false;
                    break;
                }
            }
            if (tempFlag == false) {
                flag = true;
            } else {
                flag = false;
            }

        } while (flag);
        result.add(print(bs, a, m));

        return result;
    }

    private static int[] print(int[] bs, int[] a, int m) {
        int[] result = new int[m];
        int pos = 0;
        for (int i = 0; i < bs.length; i++) {
            if (bs[i] == 1) {
                result[pos] = a[i];
                pos++;
            }
        }
        return result;
    }

    private static void print(List l) {
        for (int i = 0; i < l.size(); i++) {
            int[] a = (int[]) l.get(i);
            for (int j = 0; j < a.length; j++) {
                System.out.print(a[j] + "\t");
            }
            System.out.println();
        }
    }
}

