package ntu.mdp.group.three.utility;

import ntu.mdp.group.three.config.ArenaConfig;

import java.io.FileWriter;
import java.io.IOException;

public class AlgorithmUtils {

    public static String convertMDFToMap(String mdf2) {
        // Input MDF P2 string
        StringBuilder mdf2BinBuilder = new StringBuilder();
        StringBuilder mapBuilder = new StringBuilder();

        for (int i = 0; i < mdf2.length() - 1; i++) {
            char cur = mdf2.charAt(i);
            int cur_int = Integer.parseInt(String.valueOf(cur), 16);
            String cur_bin = Integer.toBinaryString(cur_int);
            if (cur_bin.length() < 4) {
                cur_bin = "0".repeat(4-cur_bin.length()) +cur_bin;
//                cur_bin = StringUtils.repeat("0", 4-cur_bin.length()) +cur_bin;
            }
            mdf2BinBuilder.append(cur_bin);
        }

        System.out.println(mdf2.length());
        // MDF P2 string in binary
        String mdf2_bin = mdf2BinBuilder.toString();
        System.out.println(mdf2_bin.length());

        for (int row = 0; row < ArenaConfig.ARENA_HEIGHT; row++) {
            for (int col = 0; col < ArenaConfig.ARENA_WIDTH; col++) {
                int i = row + col * 15;

                if (row <= 2 && col <= 2) {
                    mapBuilder.append('S');
                } else if (row >= 12 && col >= 17 ) {
                    mapBuilder.append('E');
                } else {
                    if (mdf2_bin.charAt(i) == '1') mapBuilder.append('O');
                    else mapBuilder.append('U');
                }
            }
            mapBuilder.append("\n");
        }
        // MDF P2 string in 15*20 map format
        String map = mapBuilder.toString();

        // write the map to a txt file named arena.txt
        try (FileWriter writer = new FileWriter("arena.txt")) {
            writer.write(map);
        }
        catch(IOException e) {
            // Handle the exception
            e.printStackTrace();
        }

        return map;
    }

}
