import cn.hutool.core.io.file.FileReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class UpdateHubHost {

    public static final String AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36";
    public static final String HEADER = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9";
    public static final String BASEURL = "https://ipaddress.com/website/";
    public static final String CONFIG_NAME = "config.txt";
    public static List<String> result = new ArrayList<>();
    public int count = 0;
    public List<String> failUrl = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        new UpdateHubHost().start();
    }

    public void start() throws InterruptedException {
        List<String> configList = getProperties();
        CountDownLatch latch = new CountDownLatch(configList.size());
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (String url : configList) {
            executorService.execute(() -> {
                String host = getIpAndHost(url);
                if (host != null) {
                    result.add(host);
                    System.out.printf("%s ==> get ip success\n", url);
                } else {
                    failUrl.add(url);
                }
                latch.countDown();
            });
        }
        latch.await();
        executorService.shutdown();
        if (failUrl.size() != 0) {
            System.out.println("No information about this domain name was queried, please check it ==> " + failUrl);
        }
        // 拿到tips的index
        System.out.printf("total %s ip query completed....\n", count);
        int[] paramIndex = WriteHost.getTipsIndex();
        if (paramIndex[0] == -1|| paramIndex[1] == -1) {
            System.out.println("tips 'START' or 'END' is incomplete, please delete these changes manually and try again.");
            return;
        }
        WriteHost.writeHostInfo(result, paramIndex[0], paramIndex[1]);
    }

    private String getIpAndHost(String url) {
        try {
            Document document = getDocument(BASEURL.concat(url));
            Element ul = document.getElementsByClass("comma-separated").get(0);
            // 拼接ip和域名 eg : 140.82.112.3 github.com
            count++;
            return ul.child(0).text().concat(" ").concat(url);
        } catch (Exception e) {
            return null;
        }
    }

    private  List<String> getProperties() {
        FileReader fileReader;
        try {
            fileReader = FileReader.create(new File("config.txt"), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Configuration read failed or configuration does not exist ! ==>> config.txt");
            return new ArrayList<>();
        }
        // 排除掉注释
        return fileReader.readLines().stream().filter(url -> !url.startsWith("#")).collect(Collectors.toList());
    }

    /**
     * @param url 访问链接
     * @return 页面对象
     */
    private Document getDocument(String url) {
        try {
            return Jsoup.parse(Jsoup.connect(url).userAgent(AGENT).header("accept", HEADER).timeout(100000).execute().body());
        } catch (IOException e) {
            System.out.println("getDocument Exception: {}" + e.getMessage());
        }
        return Jsoup.parse("<body>NONE</body>");
    }
}
