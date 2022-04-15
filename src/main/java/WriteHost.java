import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WriteHost {

    final static String TIPS = "#==============================START==============================#";
    final static String END = "#================================END================================#";

    public static int[] getTipsIndex() {
        ArrayList<String> tipList = new ArrayList<String>() {{
            add("");
            add("");
            add("");
            add("# WARNING : do not write host name under this tip !!!");
            add(TIPS);
            add(END);
        }};
        int startIndex = 0;
        int endIndex = 0;
        try {
            FileReader hostReader = FileReader.create(new File("C:\\Windows\\System32\\drivers\\etc\\hosts"));
            FileWriter hostWriter = FileWriter.create(new File("C:\\Windows\\System32\\drivers\\etc\\hosts"));
            List<String> list = hostReader.readLines();
            startIndex = list.indexOf(TIPS);
            endIndex = list.indexOf(END);
            if (startIndex == -1 && endIndex == -1 ) {
                hostWriter.writeLines(tipList, true);
                list.addAll(tipList);
                startIndex = list.indexOf(TIPS);
                endIndex = list.indexOf(END);
            }
        } catch (IORuntimeException e) {
            System.out.println("Failed to read the hosts file. Please check whether you have read-write permission !");
        }
        return new int[]{startIndex, endIndex};
    }

    public static void writeHostInfo(List<String> result, int startIndex, int endIndex) {
        FileReader hostReader = FileReader.create(new File("C:\\Windows\\System32\\drivers\\etc\\hosts"));
        FileWriter hostWriter = FileWriter.create(new File("C:\\Windows\\System32\\drivers\\etc\\hosts"));
        List<String> lines = hostReader.readLines();
        // 先清除掉之前的数据
        List<String> startList = new ArrayList<>(lines.subList(0, startIndex + 1));
        List<String> endList = new ArrayList<>(lines.subList(endIndex + 1, lines.size()));
        startList.addAll(result);
        // 增加更新时间描述
        startList.add("#\t\t\t\t\t\tUpdate time : " + DateUtil.now());
        startList.add(END);
        startList.addAll(endList);
        // 回写
        hostWriter.writeLines(startList);
        System.out.println("write host success.......");
    }
}
