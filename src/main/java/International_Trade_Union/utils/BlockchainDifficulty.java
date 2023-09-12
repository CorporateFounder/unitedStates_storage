package International_Trade_Union.utils;

import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.setings.Seting;

import java.util.BitSet;
import java.util.List;

import static International_Trade_Union.utils.HashPrinter.bytesToBinary;
import static International_Trade_Union.utils.HashPrinter.countLeadingZeros;


public class BlockchainDifficulty {
  public static void printBinary(byte[] bytes) {
    for(byte b : bytes) {
      String binary = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
      System.out.print(binary);
    }
  }
  public static int getAdjustedDifficulty(Block latestBlock, List<Block> blocks,
                                          long blockInterval, int difficultyInterval) {
    
    Block prevAdjustmentBlock = getPreviousAdjustmentBlock(blocks, difficultyInterval); 

    long expectedTime = blockInterval * difficultyInterval;
    long actualTime = latestBlock.getTimestamp().getTime() - prevAdjustmentBlock.getTimestamp().getTime();

    if(actualTime < expectedTime / 2.6) {
      return prevAdjustmentBlock.getHashCompexity() + 1;
    }
    else if(actualTime > expectedTime * 1.3) {
      return prevAdjustmentBlock.getHashCompexity() - 1;
    }
    else {
      return prevAdjustmentBlock.getHashCompexity();
    }
  }

  public static int getDifficulty(List<Block> blocks, long blockInterval, int difficultyInterval) {
    
    Block latestBlock = getLatestBlock(blocks); 
    int difficulty = latestBlock.getHashCompexity();


      difficulty = getAdjustedDifficulty(latestBlock, blocks, blockInterval, difficultyInterval);


    return Math.max(1, difficulty); 
  }

  public static boolean meetsDifficulty(byte[] hash, int difficulty) {
   
    int zeroBits = countLeadingZeroBits(hash);
    return zeroBits >= difficulty;
  }

  public static boolean v2MeetsDifficulty(byte[]hash, int difficulty, long index){

    int zeroBits = 0;
     if(index > Seting.SETING_UTILS_USE_v2MeetsDifficulty){
       String binary = bytesToBinary(hash);
        zeroBits = countLeadingZeros(binary);
     }else {
       countLeadingZeroBits(hash);
     }

    return zeroBits == difficulty;
  }

  private static Block getLatestBlock(List<Block> blocks) {
    return blocks.get(blocks.size() - 1);
  }

  private static Block getPreviousAdjustmentBlock(List<Block> blocks, int difficultyInterval) {
    return blocks.get(blocks.size() - difficultyInterval);
  }

    public static int countLeadingZeroBits(byte[] hash) {
        int bitLength = hash.length * 8;
        BitSet bits = BitSet.valueOf(hash);

        int count = 0;
        while (count < bitLength && !bits.get(count)) {
            count++;
        }

        return count;
    }

}