package International_Trade_Union.utils;



import International_Trade_Union.setings.Seting;

import java.io.IOException;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilsUse {
    private static MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
//    одно число от другого в процентах
    public static Double percentDifferent(Double first, Double second){
        return (first / second - 1) * Seting.HUNDRED_PERCENT;
    }

    //найти моду
    public static int mode(List<Integer> array)
    {
        HashMap<Integer,Integer> hm = new HashMap<Integer,Integer>();
        int max  = 1;
        int temp = 0;

        for(int i = 0; i < array.size(); i++) {

            if (hm.get(array.get(i)) != null) {

                int count = hm.get(array.get(i));
                count++;
                hm.put(array.get(i), count);

                if(count > max) {
                    max  = count;
                    temp = array.get(i);
                }
            }

            else
                hm.put(array.get(i),1);
        }
        return temp;
    }

    public static BigDecimal percentDifferent(BigDecimal first, BigDecimal second){
        return first.divide(second).subtract(new BigDecimal(1)).multiply(new BigDecimal(Seting.HUNDRED_PERCENT));
    }

    public static byte[] sha256(String text){
        return digest.digest(text.getBytes(StandardCharsets.UTF_8));
    }
    public static String sha256hash(String text){
        byte[] bytes = sha256(text);
        return bytesToHex(bytes);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String generateRandomStr() {
        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));

        return generatedString;
    }

    public static double countPercents(double sum, double percent){
        return sum * percent / Seting.HUNDRED_PERCENT;
    }
    public static BigDecimal countPercents(BigDecimal sum, BigDecimal percent){
        return sum.multiply(percent).divide(new BigDecimal(Seting.HUNDRED_PERCENT));
    }

    public  static double countGrowth(long block, double percent, double money){
        long year = (long) (block / Seting.COUNT_BLOCK_IN_DAY / (Seting.YEAR / Seting.HALF_YEAR));
        double opeartion1 = 1+ (percent / Seting.HALF_YEAR)/Seting.HUNDRED_PERCENT;
        double operation2 = Math.pow(opeartion1, year);
        double result = money * operation2;
        return result;
    }

    public static boolean hashComplexity(String literral, int hashComplexity){

        String regex = "^[0]{" + Integer.toString(hashComplexity) + "}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(literral);
        return matcher.find();
    }

    public static String hashComplexityStr(String str, int hashComplexity) throws IOException {
        int randomNumberProof = 0;
        String hash = "";
        while (true){
            randomNumberProof++;
            hash = UtilsUse.sha256hash(UtilsJson.objToStringJson(str + randomNumberProof));
            if(UtilsUse.hashComplexity(hash.substring(0, hashComplexity), hashComplexity))
            {
                break;
            }

        }
        return hash;
    }

    //для филтрации в стриме, чтобы получить уникальные обекты по полям
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
    //подсчитать количество нулей идущих подряд в hash
    public static long hashCount(String hash) {
        long count = 0;
        for (int i = 0; i < hash.length(); i++) {
            if(hash.charAt(i) == '0') count++;
            else return count;
        }
        return count;
    }

    //подсчитывает долю в процентах одного числа от другого
    public static double percentageShare(double first, double allNumber){
        return (first/allNumber)*Seting.HUNDRED_PERCENT;
    }

    //опреледеляет ближайщее число к году
    public static long nearestDateToYear(long block){
        long period = (long) (Seting.COUNT_BLOCK_IN_DAY * Seting.YEAR);
        return block / period * period;
    }


}
