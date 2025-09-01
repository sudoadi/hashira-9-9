import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class Main {

    static final class Frac {
        BigInteger num = BigInteger.ZERO;
        BigInteger den = BigInteger.ONE;
        Frac() {}
        Frac(BigInteger n) { this.num = n; this.den = BigInteger.ONE; }
        Frac(BigInteger n, BigInteger d) {
            if (d.signum() == 0) throw new IllegalArgumentException("Zero denominator");
            this.num = n; this.den = d; normalize();
        }
        void normalize() {
            if (den.signum() < 0) { den = den.negate(); num = num.negate(); }
            BigInteger g = num.abs().gcd(den);
            if (!g.equals(BigInteger.ZERO) && !g.equals(BigInteger.ONE)) {
                num = num.divide(g);
                den = den.divide(g);
            }
        }
        Frac add(Frac rhs) {
            BigInteger n = num.multiply(rhs.den).add(rhs.num.multiply(den));
            BigInteger d = den.multiply(rhs.den);
            return new Frac(n, d);
        }
        Frac mul(Frac rhs) {
            BigInteger n = num.multiply(rhs.num);
            BigInteger d = den.multiply(rhs.den);
            return new Frac(n, d);
        }
    }

    static int digitVal(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'z') return 10 + (c - 'a');
        if (c >= 'A' && c <= 'Z') return 10 + (c - 'A');
        return -1;
    }

    static BigInteger parseInBase(String s, int base) {
        BigInteger b = BigInteger.valueOf(base);
        BigInteger acc = BigInteger.ZERO;
        for (char c : s.toCharArray()) {
            int d = digitVal(c);
            acc = acc.multiply(b).add(BigInteger.valueOf(d));
        }
        return acc;
    }

    static final class CaseData {
        int n, k;
        List<Long> xs = new ArrayList<>();
        List<BigInteger> ys = new ArrayList<>();
    }

    static CaseData readTestCase(String filePath) throws IOException {
        String s = Files.readString(Path.of(filePath));
        CaseData cd = new CaseData();
        Matcher mn = Pattern.compile("\"n\"\\s*:\\s*(\\d+)").matcher(s);
        Matcher mk = Pattern.compile("\"k\"\\s*:\\s*(\\d+)").matcher(s);
        if (mn.find()) cd.n = Integer.parseInt(mn.group(1));
        if (mk.find()) cd.k = Integer.parseInt(mk.group(1));
        Pattern item = Pattern.compile("\"(\\d+)\"\\s*:\\s*\\{[^}]*?\"base\"\\s*:\\s*\"(\\d+)\"\\s*,\\s*\"value\"\\s*:\\s*\"([0-9a-zA-Z]+)\"[^}]*\\}");
        Matcher m = item.matcher(s);
        List<long[]> rows = new ArrayList<>();
        List<String> vals = new ArrayList<>();
        while (m.find()) {
            long x = Long.parseLong(m.group(1));
            int base = Integer.parseInt(m.group(2));
            String val = m.group(3);
            rows.add(new long[]{x, base});
            vals.add(val);
        }
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) idx.add(i);
        idx.sort(Comparator.comparingLong(i2 -> rows.get(i2)[0]));
        for (int i : idx) {
            cd.xs.add(rows.get(i)[0]);
            cd.ys.add(parseInBase(vals.get(i), (int) rows.get(i)[1]));
        }
        return cd;
    }

    static Frac lagrangeF0(List<Long> xs, List<BigInteger> ys) {
        int n = xs.size();
        Frac f0 = new Frac(BigInteger.ZERO);
        for (int i = 0; i < n; i++) {
            BigInteger xi = BigInteger.valueOf(xs.get(i));
            BigInteger yi = ys.get(i);
            Frac Li0 = new Frac(BigInteger.ONE, BigInteger.ONE);
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                BigInteger xj = BigInteger.valueOf(xs.get(j));
                BigInteger num = xj.negate();
                BigInteger den = xi.subtract(xj);
                Li0 = Li0.mul(new Frac(num, den));
            }
            f0 = f0.add(new Frac(yi, BigInteger.ONE).mul(Li0));
        }
        return f0;
    }

    public static void main(String[] args) throws Exception {
        String filePath = "test_case.json";
        if (args.length > 0) filePath = args[0];
        CaseData cd = readTestCase(filePath);
        Frac f0 = lagrangeF0(cd.xs, cd.ys);
        if (!f0.den.equals(BigInteger.ONE)) {
            System.out.println(f0.num + "/" + f0.den);
        } else {
            System.out.println(f0.num.toString());
        }
    }
}
