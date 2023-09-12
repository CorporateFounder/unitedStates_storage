package International_Trade_Union.utils;

public class HashPrinter {
    public static void printBinary(byte[] hash) {

        String binary = bytesToBinary(hash);

        int leadingZeros = countLeadingZeros(binary);

        System.out.println("Leading zeros: " + leadingZeros);

        for(int i = 0; i < binary.length(); i+=8) {
            System.out.print(binary.substring(i, i+8) + " ");
        }

    }

    static String bytesToBinary(byte[] bytes) {
        String binary = "";
        for(byte b : bytes) {
            binary += String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
        }
        return binary;
    }

    public static int countLeadingZeros(String binary) {

        int count = 0;
        for(int i = 0; i < binary.length(); i++) {
            if(binary.charAt(i) == '0') {
                count++;
            } else {
                break;
            }
        }

        return count;
    }

    public static void main(String[] args) {

        byte[] hash = {
                (byte)0xf6, (byte)0xdd, (byte)0xd9, (byte)0x97,
                (byte)0xaf, (byte)0x44, (byte)0x45, (byte)0x36
        };

        byte[] hash2 = "0f6ddd9975af444536471e4e4c24c9a0b59e2e13b09c42e3c609dddad199f72e".getBytes();
        printBinary(hash);
        System.out.println("***********************");
        printBinary(hash2);
        System.out.println("********************************");
        BlockchainDifficulty.printBinary(hash2);

    }
}
