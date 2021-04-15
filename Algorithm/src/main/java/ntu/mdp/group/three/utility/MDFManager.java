package ntu.mdp.group.three.utility;

import ntu.mdp.group.three.config.ArenaConfig;
import ntu.mdp.group.three.config.RobotConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class MDFManager {

    public final static int FASTEST_PATH_LENGTH = 304;

    // static variable single_instance of type Singleton
    private static MDFManager mdfManagerInstance = null;

    public boolean isExploration = false;

    // variable of type String
    public String p2String = "0000000000000000000000000000000000000000000000000000000000000000000000000000";
    public String p1String = "0000000000000000000000000000000000000000000000000000000000000000000000000000";
    public String convertedMDFString;

    public int length;

    // static method to create instance of Singleton class
    public static MDFManager getInstance()
    {
        if (mdfManagerInstance == null)
            mdfManagerInstance = new MDFManager();

        return mdfManagerInstance;
    }

    public String getP2String() {
        return p2String;
    }

    public void setP2String(String p2String) {
        this.p2String = p2String;
    }

    public String getP1String(boolean isExploration) {
        return isExploration ? "0000000000000000000000000000000000000000000000000000000000000000000000000000":
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
    }

    public void setP1String(String p1String) {
        this.p1String = p1String;
    }

    public String getConvertedMDFString() {
        return convertedMDFString;
    }

    public void setConvertedMDFString(String convertedMDFString) {
        this.convertedMDFString = convertedMDFString;
    }

    public boolean isExploration() {
        return isExploration;
    }

    public void setExploration(boolean exploration) {
        isExploration = exploration;
    }

    public int getP1Length() {
        return this.getP1String(this.isExploration).length();
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int incrementLength() {
        return this.length++;
    }

    public String getMDFCommandString() {
        return "M{\"map\":[{\"explored\": \"" + this.getP1String(this.isExploration()) +
                "\",\"length\":304,\"obstacle\":\"" + this.getP2String() + "\"}]}";
    }

    public String[][] getGridFromMDF() throws IOException {
        String[][] grid = new String[ArenaConfig.ARENA_WIDTH][ArenaConfig.ARENA_HEIGHT];
        BufferedReader bufferedReader = new BufferedReader(new StringReader(AlgorithmUtils.convertMDFToMap(this.getP2String())));
        String line;
        int j = 0;
        while ((line = bufferedReader.readLine()) != null ) {
            String[] mapChars = line.split("(?!^)");
            for (int i = 0; i < mapChars.length; i++) {
                switch(line.charAt(i)) {
                    case 'S': // Represent start grid
                        grid[i][j] = RobotConfig.START_POINT;
                        break;

                    case 'U': // Represent empty grid as end grid already used 'E'
                        // Here, we set to explored instead of Unexplored
                        grid[i][j] = RobotConfig.EXPLORED;
                        break;
                    case 'E': // Represent end grid
                        grid[i][j] = RobotConfig.END_POINT;
                        break;

                    case 'O': // Represent obstacle grid
                        grid[i][j] = RobotConfig.OBSTACLE;
                        break;
                }
            }
            j++;
        }
        return grid;
    }
}
