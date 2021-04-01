
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Main implements MainLogic {

    private final static String address = "C:\\Users\\User\\Desktop\\Grigor.gdt";

    private String FINAL_SHIFTED_RIGHT_COLUMN;
    private String middle_column = "";
    private String right_column = "";


    class AsyncMiddle implements Callable<String> {
        String[] changing = middle_column.split("\n");
        private Integer multiplier;

        public AsyncMiddle(int multiplier) {

            this.multiplier = multiplier;
        }

        @Override
        public String call() throws Exception {

            return breedMiddleColumn(changing, multiplier);
        }
    }


    private static String READ;
    private static String main[];

    static {
        String main_4x4_layouts = "";
        try {
            READ = FileHandler.getInstance().read(address);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        int offset = READ.indexOf("SRAM2RW4x4");
        int endpoint = READ.indexOf("SRAM2RW4x8");
        String layoutsContainingSub = READ.substring(offset, endpoint);
        Pattern pattern = Pattern.compile(NumberPattern.layout_pattern);
        Matcher matcher = pattern.matcher(layoutsContainingSub);//arr2[0]
        while (matcher.find()) {
            main_4x4_layouts += matcher.group() + "\n";
        }

        main = main_4x4_layouts.split("\n");

    }


    static class InvalidWordOrBitInputs extends Exception {
        public InvalidWordOrBitInputs(String message) {
            super(message);
        }

        public static void isValid(boolean ex, String message) throws InvalidWordOrBitInputs {
            if (ex) {
                throw new InvalidWordOrBitInputs(message);
            }
        }
    }

    private String asynchronousCallForMiddleColumn(int bit) throws ExecutionException, InterruptedException {
        int num = bit / 4;
        int thread_n = 10;
        ExecutorService executor = Executors.newFixedThreadPool(thread_n);
        List<AsyncMiddle> list = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            AsyncMiddle r = new AsyncMiddle(i);
            list.add(r);
        }
        List<Future<String>> future = null;
        future = executor.invokeAll(list);
        System.out.println("almost there hang on a bit...");
        executor.shutdown();
        StringBuilder current = new StringBuilder();
        for (long i = 0; i < future.size(); i++) {
            current.append(future.get((int) i).get());
        }

        return current.toString();

    }


    private String shiftRightColumn(String right_column, int word, int bit) {

        int num = (bit / 4) - 1;
        final String[] shifting = right_column.split("\n");
        String[] finalChanged = new String[shifting.length];
        String FINAL_RIGHT_COLUMN = "";
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);

        for (int i = 0; i < shifting.length; i++) {
            String numsXY = shifting[i].replaceAll(NumberPattern.num_pattern, "").split(" ", 2)[1];
            double x = Double.parseDouble(numsXY.split(" ")[0]);
            double y = Double.parseDouble(numsXY.split(" ")[1]);
            double finalX = x + num * ShiftSize.dx;
            String fx = nf.format(finalX);
            String fy = nf.format(y);
            String first_part = shifting[i].split(" xy")[0];
            finalChanged[i] = first_part + " " + "xy(" + fx + " " + fy + ")}";
            FINAL_RIGHT_COLUMN += finalChanged[i] + "\n";
        }
        return FINAL_RIGHT_COLUMN;

    }


    private String breedMiddleColumn(String[] changing, int d) {
        String FINAL_MIDDLE_COLUMN = "";
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        for (int i = 0; i < changing.length; i++) {
            String numsXY = changing[i].replaceAll(NumberPattern.num_pattern, "").split(" ", 2)[1];
            double x = Double.parseDouble(numsXY.split(" ")[0]);
            double y = Double.parseDouble(numsXY.split(" ")[1]);
            double finalX = x + d * ShiftSize.dx;
            String first_part = changing[i].split(" xy")[0];
            FINAL_MIDDLE_COLUMN += first_part + " " + "xy(" + nf.format(finalX) + " " + nf.format(y) + ")}" + "\n";

        }

        return FINAL_MIDDLE_COLUMN;
    }


    private String breedMatrices(String main_matrix, int word) {
        StringBuilder final_matrices = new StringBuilder();
        String first_part = main_matrix.split(" xy")[0];
        int num = word / 4;
        String matrix_nums = main_matrix.replaceAll(NumberPattern.num_pattern, "").split(" ", 2)[1];
        double xmatrix = Double.parseDouble(matrix_nums.split(" ")[0]);
        double ymatrix = Double.parseDouble(matrix_nums.split(" ")[1]);

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);

        for (int i = 0; i < num; i++) {
            double finalY = ymatrix + i * ShiftSize.dy;
            String fy = nf.format(finalY);
            String fx = nf.format(xmatrix);
            final_matrices.append(first_part + " " + "xy(" + fx + " " + fy + ")}" + "\n");
        }

        return final_matrices.toString();
    }


    private String[] topTripletPulUp(int word) {
        String[] top_triplet = {main[MainPosition.left_top], main[MainPosition.middle_top], main[MainPosition.right_top]};
        String finalTopHorizontalRow[] = new String[top_triplet.length];
        int matrix_number = word / 4;
        int endY_multiply = matrix_number - 1;

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);

        for (int i = 0; i < top_triplet.length; i++) {
            String numXy = top_triplet[i].replaceAll(NumberPattern.num_pattern, "").split(" ", 2)[1];
            double x = Double.parseDouble(numXy.split(" ")[0]);
            double y = Double.parseDouble(numXy.split(" ")[1]);
            double finalY = y + endY_multiply * ShiftSize.dy;
            String fxx = nf.format(x);
            String fy = nf.format(finalY);
            finalTopHorizontalRow[i] = top_triplet[i].split(" xy")[0] + " " + "xy(" + fxx + " " + fy + ")}" + "\n";

        }

        return finalTopHorizontalRow;
    }


    private String makeAllChanges(int word, int bit) throws ExecutionException, InterruptedException {
        String FINAL_LEFT_COLUMN = "";
        String finalTopHorizontalRow[] = new String[3];
        String final_matrices;
        int top_triplet_right = 2;
        int top_triplet_middle = 1;
        int top_triplet_left = 0;
        int leftNum = AsideSolution.log2(word);//3 4 5 6
        String left_decoder = Decoders.getLeftByName(Integer.toString(leftNum), Integer.toString(word));
        String right_decoder = Decoders.getRightByName(Integer.toString(leftNum), Integer.toString(word));

        finalTopHorizontalRow = topTripletPulUp(word);
        final_matrices = breedMatrices(main[MainPosition.middle_below_top], word);

        middle_column += finalTopHorizontalRow[top_triplet_middle] + final_matrices + main[MainPosition.middle_above_bottom] + "\n" +
                main[MainPosition.middle_bottom] + "\n";
        right_column += finalTopHorizontalRow[top_triplet_right] + right_decoder + "\n" + main[MainPosition.right_above_bottom] + "\n" +
                main[MainPosition.right_bottom] + "\n";


        String MIDDLE_FINAL_COLUMN = asynchronousCallForMiddleColumn(bit);//


        Thread t2 = new Thread() {
            @Override
            public void run() {
                FINAL_SHIFTED_RIGHT_COLUMN = shiftRightColumn(right_column, word, bit);
            }
        };

        t2.start();
        try {

            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        FINAL_LEFT_COLUMN += finalTopHorizontalRow[top_triplet_left] + left_decoder + "\n" + main[MainPosition.left_above_bottom] + "\n" + main[MainPosition.left_bottom] + "\n";
        return MIDDLE_FINAL_COLUMN + FINAL_LEFT_COLUMN + FINAL_SHIFTED_RIGHT_COLUMN;

    }


    @Override
    public void buildLayouts() {
        Scanner in = new Scanner(System.in);
        System.out.println("write down word_line size ");
        int word = in.nextInt();
        System.out.println("write down bit_line size");
        int bit = in.nextInt();
        try {
            InvalidWordOrBitInputs.isValid(word < 4 || bit < 4, "invalid inputs");
            InvalidWordOrBitInputs.isValid(word % 4 != 0 || bit % 4 != 0, "invalid inputs");
            InvalidWordOrBitInputs.isValid(word > 1024, "invalid inputs");
            InvalidWordOrBitInputs.isValid(AsideSolution.check_log2(word) % 1 != 0, "wrong word size,can't find decoder");
        } catch (InvalidWordOrBitInputs e) {
            System.out.println(e.getMessage());
            System.out.println("try again later");
            System.exit(1);
        }
        System.out.println("please wait....");
        String needed = AsideSolution.removeLastCharacter(READ);
        try {
            FileHandler.getInstance().writeWithoutAppend(address, needed);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        String full_design;

        String cell_name = AsideSolution.SRAM_name_generator(word, bit);
        long start = new Date().getTime();
        String cell_layouts = null;
        try {
            cell_layouts = makeAllChanges(word, bit);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
        long end = new Date().getTime();
        String extension = "}" + "\n}" + "\n";
        full_design = cell_name + "\n" + cell_layouts + extension;
        try {
            FileHandler.getInstance().writeWithAppend(address, full_design);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println(Message.SUCCESS_MESSAGE);
        System.out.println("needed time in sec : " + (end - start) / 1000);
        System.exit(0);
    }


    public static void main(String[] args) {



    }
}

class PinsHandler implements PinsHandlerPlan {

    private final double distanceBetweenTwoPoints = 31.78;

    @Override
    public String generateRightBaseAndRect(int word, int bit) {
        String result = "";
        String planType = "Dec" + word;
        int rollNo = AsideSolution.log2(word);
        PlanFactory planFactory = new PlanFactory();
        Plan plan = planFactory.getPlan(planType);
        Map<String, Double> points = plan.getPointXY();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        int shiftX = (bit / 4) - 1;
        for (int i = 0; i <= rollNo; i++) {
            double pointX = (points.get("pointx" + i) + distanceBetweenTwoPoints + shiftX * ShiftSize.dx);
            double pointY = points.get("pointy" + i);
//rect
            double x1r = (pointX - LeftRectangle.half_of_a_rect_length);//rect
            double y1r = (pointY - LeftRectangle.half_of_a_rect_width);//rect
//base
            double xb1 = (pointX - Base.half_of_base_length);
            double xb2 = (pointX + Base.half_of_base_length);
            double xb3 = xb2;
            double xb4 = xb1;
            double yb1 = (pointY - Base.half_of_base_length);
            double yb2 = yb1;
            double yb3 = (pointY + Base.half_of_base_width);
            double yb4 = yb3;
            String rect = "t{41 mc m0.2 xy(" + nf.format(x1r) + " " + nf.format(y1r) + ")" + " " + "'A2<" + i + ">'}";
            String base = "b{41 xy(" + nf.format(xb1) + " " + nf.format(yb1) + " " + nf.format(xb2) + " " + nf.format(yb2) + " " +
                    nf.format(xb3) + " " + nf.format(yb3) + " " + nf.format(xb4) + " " + nf.format(yb4) + ")}";
            result += rect + "\n" + base + "\n";
        }

        return result;

    }


    @Override
    public String generateLeftBaseAndRect(int word) {
        String result = "";
        String planType = "Dec" + word;
        int rollNo = AsideSolution.log2(word);
        PlanFactory planFactory = new PlanFactory();
        Plan plan = planFactory.getPlan(planType);
        Map<String, Double> points = plan.getPointXY();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        for (int i = 0; i <= rollNo; i++) {
            double pointX = points.get("pointx" + i);
            double pointY = points.get("pointy" + i);
//rect
            double x1r = (pointX - LeftRectangle.half_of_a_rect_length);//rect
            double y1r = (pointY - LeftRectangle.half_of_a_rect_width);//rect
//base
            double xb1 = (pointX - Base.half_of_base_length);
            double xb2 = (pointX + Base.half_of_base_length);
            double xb3 = xb2;
            double xb4 = xb1;
            double yb1 = (pointY - Base.half_of_base_length);
            double yb2 = yb1;
            double yb3 = (pointY + Base.half_of_base_width);
            double yb4 = yb3;
            String rect = "t{41 mc m0.2 xy(" + nf.format(x1r) + " " + nf.format(y1r) + ")" + " " + "'A1<" + i + ">'}";
            String base = "b{41 xy(" + nf.format(xb1) + " " + nf.format(yb1) + " " + nf.format(xb2) + " " + nf.format(yb2) + " " +
                    nf.format(xb3) + " " + nf.format(yb3) + " " + nf.format(xb4) + " " + nf.format(yb4) + ")}";
            result += rect + "\n" + base + "\n";
        }

        return result;
    }

    static abstract class Plan {
        protected Map<String, Double> pointXY;

        abstract Map<String, Double> getPointXY();

    }

    public static class Dec4 extends Plan {

        public Dec4() {// 01
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(0));
            pointXY.put("pointx1", new Double(0));
            pointXY.put("pointx2", new Double(1));
            pointXY.put("pointy0", new Double(0));
            pointXY.put("pointy1", new Double(0));
            pointXY.put("pointy2", new Double(1));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec8 extends Plan {//012

        public Dec8() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(0));
            pointXY.put("pointx1", new Double(0));
            pointXY.put("pointx2", new Double(0));
            pointXY.put("pointx3", new Double(1));
            pointXY.put("pointy0", new Double(0));
            pointXY.put("pointy1", new Double(0));
            pointXY.put("pointy2", new Double(0));
            pointXY.put("pointy3", new Double(1));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec16 extends Plan {//0123

        public Dec16() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(0));
            pointXY.put("pointx1", new Double(0));
            pointXY.put("pointx2", new Double(0));
            pointXY.put("pointx3", new Double(0));
            pointXY.put("pointx4", new Double(1));
            pointXY.put("pointy0", new Double(0));
            pointXY.put("pointy1", new Double(0));
            pointXY.put("pointy2", new Double(0));
            pointXY.put("pointy3", new Double(0));
            pointXY.put("pointy4", new Double(1));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec32 extends Plan {//01234

        public Dec32() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(0));
            pointXY.put("pointx1", new Double(0));
            pointXY.put("pointx2", new Double(0));
            pointXY.put("pointx3", new Double(0));
            pointXY.put("pointx4", new Double(0));
            pointXY.put("pointx5", new Double(1));
            pointXY.put("pointy0", new Double(0));
            pointXY.put("pointy1", new Double(0));
            pointXY.put("pointy2", new Double(0));
            pointXY.put("pointy3", new Double(0));
            pointXY.put("pointy4", new Double(0));
            pointXY.put("pointy5", new Double(1));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec64 extends Plan {//12345

        public Dec64() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(0));
            pointXY.put("pointx1", new Double(0));
            pointXY.put("pointx2", new Double(0));
            pointXY.put("pointx3", new Double(0));
            pointXY.put("pointx4", new Double(0));
            pointXY.put("pointx5", new Double(0));
            pointXY.put("pointx6", new Double(1));
            pointXY.put("pointy0", new Double(0));
            pointXY.put("pointy1", new Double(0));
            pointXY.put("pointy2", new Double(0));
            pointXY.put("pointy3", new Double(0));
            pointXY.put("pointy4", new Double(0));
            pointXY.put("pointy5", new Double(0));
            pointXY.put("pointy6", new Double(1));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec128 extends Plan {//0123456

        public Dec128() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(0));
            pointXY.put("pointx1", new Double(0));
            pointXY.put("pointx2", new Double(0));
            pointXY.put("pointx3", new Double(0));
            pointXY.put("pointx4", new Double(0));
            pointXY.put("pointx5", new Double(0));
            pointXY.put("pointx6", new Double(0));
            pointXY.put("pointx7", new Double(1));
            pointXY.put("pointy0", new Double(0));
            pointXY.put("pointy1", new Double(0));
            pointXY.put("pointy2", new Double(0));
            pointXY.put("pointy3", new Double(0));
            pointXY.put("pointy4", new Double(0));
            pointXY.put("pointy5", new Double(0));
            pointXY.put("pointy6", new Double(0));
            pointXY.put("pointy7", new Double(1));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec256 extends Plan {//01234567

        public Dec256() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(0));
            pointXY.put("pointx1", new Double(0));
            pointXY.put("pointx2", new Double(0));
            pointXY.put("pointx3", new Double(0));
            pointXY.put("pointx4", new Double(0));
            pointXY.put("pointx5", new Double(0));
            pointXY.put("pointx6", new Double(0));
            pointXY.put("pointx7", new Double(1));
            pointXY.put("pointx8", new Double(1));
            pointXY.put("pointy0", new Double(0));
            pointXY.put("pointy1", new Double(0));
            pointXY.put("pointy2", new Double(0));
            pointXY.put("pointy3", new Double(0));
            pointXY.put("pointy4", new Double(0));
            pointXY.put("pointy5", new Double(0));
            pointXY.put("pointy6", new Double(0));
            pointXY.put("pointy7", new Double(1));
            pointXY.put("pointy8", new Double(1));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec512 extends Plan {
        public Dec512() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(0));
            pointXY.put("pointx1", new Double(0));
            pointXY.put("pointx2", new Double(0));
            pointXY.put("pointx3", new Double(0));
            pointXY.put("pointx4", new Double(0));
            pointXY.put("pointx5", new Double(0));
            pointXY.put("pointx6", new Double(0));
            pointXY.put("pointx7", new Double(1));
            pointXY.put("pointx8", new Double(1));
            pointXY.put("pointx9", new Double(1));
            pointXY.put("pointy0", new Double(0));
            pointXY.put("pointy1", new Double(0));
            pointXY.put("pointy2", new Double(0));
            pointXY.put("pointy3", new Double(0));
            pointXY.put("pointy4", new Double(0));
            pointXY.put("pointy5", new Double(0));
            pointXY.put("pointy6", new Double(0));
            pointXY.put("pointy7", new Double(1));
            pointXY.put("pointy8", new Double(1));
            pointXY.put("pointy9", new Double(1));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec1024 extends Plan {
        public Dec1024() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(0));
            pointXY.put("pointx1", new Double(0));
            pointXY.put("pointx2", new Double(0));
            pointXY.put("pointx3", new Double(0));
            pointXY.put("pointx4", new Double(0));
            pointXY.put("pointx5", new Double(0));
            pointXY.put("pointx6", new Double(0));
            pointXY.put("pointx7", new Double(1));
            pointXY.put("pointx8", new Double(1));
            pointXY.put("pointx9", new Double(1));
            pointXY.put("pointx10", new Double(1));
            pointXY.put("pointy0", new Double(0));
            pointXY.put("pointy1", new Double(0));
            pointXY.put("pointy2", new Double(0));
            pointXY.put("pointy3", new Double(0));
            pointXY.put("pointy4", new Double(0));
            pointXY.put("pointy5", new Double(0));
            pointXY.put("pointy6", new Double(0));
            pointXY.put("pointy7", new Double(1));
            pointXY.put("pointy8", new Double(1));
            pointXY.put("pointy9", new Double(1));
            pointXY.put("pointy10", new Double(1));

        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }


    class PlanFactory {

        public Plan getPlan(String planType) {
            if (planType == null) {
                return null;
            }
            if (planType.equalsIgnoreCase("Dec4")) {
                return new Dec4();
            } else if (planType.equalsIgnoreCase("Dec8")) {
                return new Dec8();
            } else if (planType.equalsIgnoreCase("Dec16")) {
                return new Dec16();
            } else if (planType.equalsIgnoreCase("Dec32")) {
                return new Dec32();
            } else if (planType.equalsIgnoreCase("Dec64")) {
                return new Dec64();
            } else if (planType.equalsIgnoreCase("Dec128")) {
                return new Dec128();
            } else if (planType.equalsIgnoreCase("Dec256")) {
                return new Dec256();
            } else if (planType.equalsIgnoreCase("Dec512")) {
                return new Dec512();
            } else if (planType.equalsIgnoreCase("Dec1024")) {
                return new Dec1024();
            }

            return null;
        }
    }


}

interface PinsHandlerPlan {


    String generateRightBaseAndRect(int word, int bit);

    String generateLeftBaseAndRect(int word);
}

class Base {
    public static double half_of_base_length = 0.08;
    public static double half_of_base_width = 0.04;
}

class LeftRectangle {
    public static double half_of_a_rect_length = 0.3505;
    public static double half_of_a_rect_width = 0.168;
}

class FileHandler implements FileInOutProtocol {

    private static final FileHandler INSTANCE = new FileHandler();

    private FileHandler() {

    }

    public static FileHandler getInstance() {
        if (INSTANCE == null) {
            return new FileHandler();
        }
        return INSTANCE;
    }

    private static void exists(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(Message.STH_WRONG_MESSAGE);
            }
        }
    }

    @Override
    public String read(String address) throws IOException {
        Path path = Paths.get(address);
        StringBuilder sb = new StringBuilder();
        Files.lines(path).forEach(str -> sb.append(str + "\n"));
        return sb.toString();

    }


    @Override
    public void writeWithoutAppend(String address, String text) throws IOException {
        Files.write(Paths.get(address), text.getBytes());
    }

    @Override
    public void writeWithAppend(String address, String text) throws IOException {
        Files.write(Paths.get(address), text.getBytes(), StandardOpenOption.APPEND);

    }
}


interface FileInOutProtocol {

    String read(String address) throws IOException;

    void writeWithAppend(String address, String text) throws IOException;

    void writeWithoutAppend(String address, String text) throws IOException;

}

interface MainLogic {

    void buildLayouts() throws IOException;

}

final class AsideSolution {
    private final static String FIRST_PART = "MYSRAM2RW";

    public static String SRAM_name_generator(int words, int bits) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String celDes = "cell{c=2017-09-25 13:22:51" + " " + "m=" + dateFormat.format(new Date()) + " " + "\'" + FIRST_PART + words + "x" + bits + "\'";
        return celDes;
    }//last } does't generate

    public static String removeLastCharacter(String str) {
        String result = null;
        if ((str != null) && (str.length() > 0)) {
            result = str.substring(0, str.length() - 2);
        }
        return result;
    }

    public static int log2(int N) {

        int result = (int) (Math.log(N) / Math.log(2));

        return result;
    }

    public static double check_log2(int N) {

        double result = (double) (Math.log(N) / Math.log(2));

        return result;
    }
}

final class Decoders {
    private static final String dec2x4_left = "s{'sgd_dec2x4_left' xy(0 12.929)}";
    private static final String dec2x4_right = "s{'sgd_dec2x4_right' xy(17.783 12.929)}";

    private static final String dec3x8_left = "s{'sgd_dec3x8_left' xy(0 12.929)}";
    private static final String dec3x8_right = "s{'sgd_dec3x8_right' xy(17.783 12.929)}";

    private static final String dec4x16_left = "s{'sgd_dec4x16_left' xy(0 12.929)}";
    private static final String dec4x16_right = "s{'sgd_dec4x16_right' xy(17.783 12.929)}";

    private static final String dec5x32_left = "s{'sgd_dec5x32_left' xy(0 12.929)}";
    private static final String dec5x32_right = "s{'sgd_dec5x32_right' xy(17.783 12.929)}";

    private static final String dec6x64_left = "s{'sgd_dec6x64_left' xy(0 12.929)}";
    private static final String dec6x64_right = "s{'sgd_dec6x64_right' xy(17.783 12.929)}";

    private static final String dec7x128_left = "s{'sgd_dec7x128_left' xy(0 12.929)}";
    private static final String dec7x128_right = "s{'sgd_dec7x128_right' xy(17.783 12.929)}";

    private static final String dec8x256_left = "s{'sgd_dec_8x256_left' xy(0 12.929)}";
    private static final String dec8x256_right = "s{'sgd_dec_8x256_right' xy(17.783 12.929)}";

    private static final String dec9x512_left = "s{'sgd_dec_9X512_left' xy(0 12.929)}";
    private static final String dec9x512_right = "s{'sgd_dec_9X512_Right' xy(17.783 12.929)}";

    private static final String dec10x1024_left = "s{'sgd_dec_10X1024_left' xy(0 12.929)}";
    private static final String dec10x1024_right = "s{'sgd_dec_10X1024_Right' xy(17.783 12.929)}";

    public static String getLeftByName(String leftN, String rightNum) {
        String name = "dec" + leftN + "x" + rightNum + "_left";
        Field field = null;
        Object o = null;
        try {
            field = Decoders.class.getDeclaredField(name);
            field.setAccessible(true);
            o = field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.out.println(e.toString());
        }
        return o.toString();

    }


    public static String getRightByName(String leftN, String rightNum) {
        String name = "dec" + leftN + "x" + rightNum + "_right";
        Field field = null;
        Object o = null;
        try {
            field = Decoders.class.getDeclaredField(name);
            field.setAccessible(true);
            o = field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.out.println(e.toString());
        }
        return o.toString();
    }

}

final class Message {
    public static final String SUCCESS_MESSAGE = "finished successfully";
    public static final String STH_WRONG_MESSAGE = "something went wrong";
    public static final String WRONG_INPUTS = "wrong inputs try again...";
}

final class NumberPattern {
    public final static String layout_pattern = "s(\\{)([A-Za-z_0-9_\\s_[().,' '])]){0,}(\\})";
    public final static String num_pattern = "[^0-9.\\s+]";
}

final class MainPosition {

    public final static int middle_top = 0;
    public final static int middle_below_top = 1;
    public final static int middle_above_bottom = 4;
    public final static int middle_bottom = 3;

    public final static int right_top = 11;
    public final static int right_below_top = 5;
    public final static int right_above_bottom = 9;
    public final static int right_bottom = 10;

    public final static int left_top = 7;
    public final static int left_below_top = 8;
    public final static int left_above_bottom = 6;
    public final static int left_bottom = 2;
}

final class ShiftSize {
    public final static double dx = 3.626;
    public final static double dy = 2.48;
}