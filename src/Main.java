
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

    public interface  doNothing{

    }
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


    public static void main(String[] args) throws IllegalAccessException {
        System.out.println(new PinsHandler().getIoreg2s(8));
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
            double x1r = (pointX - LeftRightARectangle.half_of_a_rect_length);//rect
            double y1r = (pointY - LeftRightARectangle.half_of_a_rect_width);//rect
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
            double x1r = (pointX - LeftRightARectangle.half_of_a_rect_length);//rect
            double y1r = (pointY - LeftRightARectangle.half_of_a_rect_width);//rect
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


    @Override
    public String buildVssAndVdd(int word) throws IllegalAccessException {
        VssVdd object = new VssVdd();
        int EVEN_OR_ODD = AsideSolution.log2(word);
        if (EVEN_OR_ODD % 2 == 0) {
            return object.getVssAndVdd("even");
        } else if (EVEN_OR_ODD % 2 != 0) {
            return object.getVssAndVdd("odd");
        } else {
            return null;
        }
    }

    @Override
    public String getIoregOnes() {
        IoregPins pins = new IoregPins();
        return pins.getOnes();
    }

    @Override
    public String getIoreg2s(int bit) {
        return new IoregPins().get2(bit);
    }

    private String generate(int multiplier, int firstN, int secondN) {// 0 1   2  3   4  5

        return null;
    }

    public String execute(int bit) {
        int num = bit / 4;
        PinsHandler object = new PinsHandler();
        StringBuilder builder = new StringBuilder();
        for (final int[] i = {0}; i[0] < num; i[0]++) {
            int j = 0;
            int finalJ = j;
            Thread t = new Thread() {
                @Override
                public void run() {
                    builder.append(object.generate(i[0], i[0] + finalJ, ++i[0] + finalJ) + "\n");
                }
            };
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ++j;
        }
        return builder.toString();
    }


    static abstract class Plan {
        protected Map<String, Double> pointXY;

        abstract Map<String, Double> getPointXY();

    }

    public static class Dec4 extends Plan {

        public Dec4() {// 01
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(5.43));
            pointXY.put("pointx1", new Double(5.43));
            pointXY.put("pointx2", new Double(5.43));
            pointXY.put("pointy0", new Double(3.454));
            pointXY.put("pointy1", new Double(4.564));
            pointXY.put("pointy2", new Double(4.998));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec8 extends Plan {//012

        public Dec8() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(5.43));
            pointXY.put("pointx1", new Double(5.43));
            pointXY.put("pointx2", new Double(5.43));
            pointXY.put("pointx3", new Double(5.43));
            pointXY.put("pointy0", new Double(3.234));
            pointXY.put("pointy1", new Double(3.897));
            pointXY.put("pointy2", new Double(4.32));
            pointXY.put("pointy3", new Double(5.322));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec16 extends Plan {//0123

        public Dec16() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(5.43));
            pointXY.put("pointx1", new Double(5.43));
            pointXY.put("pointx2", new Double(5.43));
            pointXY.put("pointx3", new Double(5.43));
            pointXY.put("pointx4", new Double(5.43));
            pointXY.put("pointy0", new Double(2.453));
            pointXY.put("pointy1", new Double(3.211));
            pointXY.put("pointy2", new Double(4.322));
            pointXY.put("pointy3", new Double(4.765));
            pointXY.put("pointy4", new Double(5.432));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec32 extends Plan {//01234

        public Dec32() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(5.43));
            pointXY.put("pointx1", new Double(5.43));
            pointXY.put("pointx2", new Double(5.43));
            pointXY.put("pointx3", new Double(5.43));
            pointXY.put("pointx4", new Double(5.43));
            pointXY.put("pointx5", new Double(5.43));
            pointXY.put("pointy0", new Double(3.23));
            pointXY.put("pointy1", new Double(3.564));
            pointXY.put("pointy2", new Double(5.222));
            pointXY.put("pointy3", new Double(5.432));
            pointXY.put("pointy4", new Double(6.544));
            pointXY.put("pointy5", new Double(7.543));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec64 extends Plan {//12345

        public Dec64() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(5.43));
            pointXY.put("pointx1", new Double(5.43));
            pointXY.put("pointx2", new Double(5.43));
            pointXY.put("pointx3", new Double(5.43));
            pointXY.put("pointx4", new Double(5.43));
            pointXY.put("pointx5", new Double(5.43));
            pointXY.put("pointx6", new Double(5.43));
            pointXY.put("pointy0", new Double(3.444));
            pointXY.put("pointy1", new Double(5.432));
            pointXY.put("pointy2", new Double(5.999));
            pointXY.put("pointy3", new Double(6.876));
            pointXY.put("pointy4", new Double(7.433));
            pointXY.put("pointy5", new Double(8.332));
            pointXY.put("pointy6", new Double(9.443));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec128 extends Plan {//0123456

        public Dec128() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(5.43));
            pointXY.put("pointx1", new Double(5.43));
            pointXY.put("pointx2", new Double(5.43));
            pointXY.put("pointx3", new Double(5.43));
            pointXY.put("pointx4", new Double(5.43));
            pointXY.put("pointx5", new Double(5.43));
            pointXY.put("pointx6", new Double(5.43));
            pointXY.put("pointx7", new Double(5.43));
            pointXY.put("pointy0", new Double(3.2344));
            pointXY.put("pointy1", new Double(4.333));
            pointXY.put("pointy2", new Double(4.899));
            pointXY.put("pointy3", new Double(5.445));
            pointXY.put("pointy4", new Double(6.342));
            pointXY.put("pointy5", new Double(7.543));
            pointXY.put("pointy6", new Double(8.322));
            pointXY.put("pointy7", new Double(8.542));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec256 extends Plan {//01234567

        public Dec256() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(5.43));
            pointXY.put("pointx1", new Double(5.43));
            pointXY.put("pointx2", new Double(5.43));
            pointXY.put("pointx3", new Double(5.43));
            pointXY.put("pointx4", new Double(5.43));
            pointXY.put("pointx5", new Double(5.43));
            pointXY.put("pointx6", new Double(5.43));
            pointXY.put("pointx7", new Double(5.43));
            pointXY.put("pointx8", new Double(5.43));
            pointXY.put("pointy0", new Double(2.343));
            pointXY.put("pointy1", new Double(4.322));
            pointXY.put("pointy2", new Double(5.322));
            pointXY.put("pointy3", new Double(6.123));
            pointXY.put("pointy4", new Double(6.994));
            pointXY.put("pointy5", new Double(7.432));
            pointXY.put("pointy6", new Double(8.123));
            pointXY.put("pointy7", new Double(9.453));
            pointXY.put("pointy8", new Double(10.211));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec512 extends Plan {
        public Dec512() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(5.43));
            pointXY.put("pointx1", new Double(5.43));
            pointXY.put("pointx2", new Double(5.43));
            pointXY.put("pointx3", new Double(5.43));
            pointXY.put("pointx4", new Double(5.43));
            pointXY.put("pointx5", new Double(5.43));
            pointXY.put("pointx6", new Double(5.43));
            pointXY.put("pointx7", new Double(5.43));
            pointXY.put("pointx8", new Double(5.43));
            pointXY.put("pointx9", new Double(5.43));
            pointXY.put("pointy0", new Double(3.453));
            pointXY.put("pointy1", new Double(4.332));
            pointXY.put("pointy2", new Double(5.343));
            pointXY.put("pointy3", new Double(5.998));
            pointXY.put("pointy4", new Double(7.223));
            pointXY.put("pointy5", new Double(8.223));
            pointXY.put("pointy6", new Double(9.232));
            pointXY.put("pointy7", new Double(11.234));
            pointXY.put("pointy8", new Double(12.433));
            pointXY.put("pointy9", new Double(12.998));
        }


        @Override
        Map<String, Double> getPointXY() {
            return pointXY;
        }
    }

    public static class Dec1024 extends Plan {
        public Dec1024() {
            pointXY = new HashMap<>();
            pointXY.put("pointx0", new Double(5.43));
            pointXY.put("pointx1", new Double(5.43));
            pointXY.put("pointx2", new Double(5.43));
            pointXY.put("pointx3", new Double(5.43));
            pointXY.put("pointx4", new Double(5.43));
            pointXY.put("pointx5", new Double(5.43));
            pointXY.put("pointx6", new Double(5.43));
            pointXY.put("pointx7", new Double(5.43));
            pointXY.put("pointx8", new Double(5.43));
            pointXY.put("pointx9", new Double(5.43));
            pointXY.put("pointx10", new Double(5.43));
            pointXY.put("pointy0", new Double(3.22));
            pointXY.put("pointy1", new Double(4.223));
            pointXY.put("pointy2", new Double(5.322));
            pointXY.put("pointy3", new Double(6.212));
            pointXY.put("pointy4", new Double(7.211));
            pointXY.put("pointy5", new Double(7.992));
            pointXY.put("pointy6", new Double(9.132));
            pointXY.put("pointy7", new Double(11.12));
            pointXY.put("pointy8", new Double(13.894));
            pointXY.put("pointy9", new Double(14.222));
            pointXY.put("pointy10", new Double(17.322));

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

    String buildVssAndVdd(int word) throws IllegalAccessException;

    String getIoregOnes();

    String getIoreg2s(int bit);
}

final class IoregPins {
    private final String CE1;
    private final String CE1BASE;
    private final String CSB1;
    private final String CSB1BASE;
    private final String WEB1;
    private final String WEB1BASE;
    private final String OEB1;
    private final String OEB1BASE;

    private double CE2X = 31.642;
    private double CE2Y = 9.641;
    private double CSB2X = 31.563;
    private double CSB2Y = 9.301;
    private double WEB2X = 31.518;
    private double WEB2Y = 2.798;
    private double OEB2X = 31.550;
    private double OEB2Y = 8.832;

    private double CE2BASE_X = 31.861;
    private double CE2BASE_Y = 9.808;
    private double CSB2BASE_X = 31.860;
    private double CSB2BASE_Y = 9.468;
    private double WEB2BASE_X = 31.860;
    private double WEB2BASE_Y = 2.965;
    private double OEB2BASE_X = 31.860;
    private double OEB2BASE_Y = 8.999;


    IoregPins() {
        CE1 = "t{41 mc fx m0.2 a180 xy(0.081 9.808) 'CE1'}";
        CE1BASE = "b{41 xy(0 9.679 0.16 9.769 0.16 9.849 0 9.849)}";
        CSB1 = "t{41 mc m.2 xy(0.08 9.468) 'CSB1'}";
        CSB1BASE = "b{41 xy(0 9.428 0.16 9.428 0.16 9.508 0 9.508)}";
        WEB1 = "t{41 mc fx m0.2 a180 xy(0.08 2.965) 'WEB1'}";
        WEB1BASE = "b{41 xy(0 2.925 0.16 2.925 0.16 3.005 0 3.005)}";
        OEB1 = "t{41 mc m0.2 xy(0.08 8.999) 'OEB1'}";
        OEB1BASE = "b{41 xy(0 8.959 0.16 8.959 0.16 9.039 0 9.039)}";
    }

    public String getOnes() {
        String ones = "" + CE1 + "\n" + CE1BASE + "\n" + CSB1 + "\n" + CSB1BASE + "\n" + WEB1 + "\n" + WEB1BASE + "\n" + OEB1 + "\n" + OEB1BASE + "\n";
        return ones;
    }

    public String get2(int bit) {
        int m = (bit / 4) - 1;
        double add = ShiftSize.dx * m;
        String result = "";
        result += get2sCE2(add) + get2sCSB2(add) + get2sWEB2(add) + get2sOEB2(add);
        return result;
    }

    private String get2sCE2(double add) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        double ce_base_x1 = (CE2BASE_X + add) - Base.half_of_base_length;
        double ce_base_x2 = (CE2BASE_X + add) + Base.half_of_base_length;
        double ce_base_x3 = ce_base_x2;
        double ce_base_x4 = ce_base_x1;
        double ce_base_y1 = CE2BASE_Y - Base.half_of_base_width;
        double ce_base_y2 = ce_base_y1;
        double ce_base_y3 = CE2BASE_Y + Base.half_of_base_width;
        double ce_base_y4 = ce_base_y3;

        String CE2_WITH_BASE = "" + "t{41 mc fx m0.2 a180 xy(" + (nf.format(CE2X + add)) + " " + nf.format(CE2Y) + ")" + " " + "'CE2'}" + "\n" +
                "b{41 xy(" + nf.format(ce_base_x1) + " " + nf.format(ce_base_y1) + " " + nf.format(ce_base_x2) + " " +
                nf.format(ce_base_y2) + " " + nf.format(ce_base_x3) + " " + nf.format(ce_base_y3) + " " + nf.format(ce_base_x4) + " " +
                nf.format(ce_base_y4) + ")}" + "\n";
        return CE2_WITH_BASE;
    }

    private String get2sCSB2(double add) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        double ce_base_x1 = (CSB2BASE_X + add) - Base.half_of_base_length;
        double ce_base_x2 = (CSB2BASE_X + add) + Base.half_of_base_length;
        double ce_base_x3 = ce_base_x2;
        double ce_base_x4 = ce_base_x1;
        double ce_base_y1 = CSB2BASE_Y - Base.half_of_base_width;
        double ce_base_y2 = ce_base_y1;
        double ce_base_y3 = CSB2BASE_Y + Base.half_of_base_width;
        double ce_base_y4 = ce_base_y3;

        String CSB2_WITH_BASE = "" + "t{41 mc m0.2 xy(" + (nf.format(CSB2X + add)) + " " + nf.format(CSB2Y) + ")" + " " + "'CSB2'}" + "\n" +
                "b{41 xy(" + nf.format(ce_base_x1) + " " + nf.format(ce_base_y1) + " " + nf.format(ce_base_x2) + " " +
                nf.format(ce_base_y2) + " " + nf.format(ce_base_x3) + " " + nf.format(ce_base_y3) + " " + nf.format(ce_base_x4) + " " +
                nf.format(ce_base_y4) + ")}" + "\n";
        return CSB2_WITH_BASE;
    }

    private String get2sWEB2(double add) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        double ce_base_x1 = (WEB2BASE_X + add) - Base.half_of_base_length;
        double ce_base_x2 = (WEB2BASE_X + add) + Base.half_of_base_length;
        double ce_base_x3 = ce_base_x2;
        double ce_base_x4 = ce_base_x1;
        double ce_base_y1 = WEB2BASE_Y - Base.half_of_base_width;
        double ce_base_y2 = ce_base_y1;
        double ce_base_y3 = WEB2BASE_Y + Base.half_of_base_width;
        double ce_base_y4 = ce_base_y3;

        String WEB2_WITH_BASE = "" + "t{41 mc fx m0.2 a180 xy(" + (nf.format(WEB2X + add)) + " " + nf.format(WEB2Y) + ")" + " " + "'WEB2'}" + "\n" +
                "b{41 xy(" + nf.format(ce_base_x1) + " " + nf.format(ce_base_y1) + " " + nf.format(ce_base_x2) + " " +
                nf.format(ce_base_y2) + " " + nf.format(ce_base_x3) + " " + nf.format(ce_base_y3) + " " + nf.format(ce_base_x4) + " " +
                nf.format(ce_base_y4) + ")}" + "\n";
        return WEB2_WITH_BASE;
    }

    private String get2sOEB2(double add) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        double ce_base_x1 = (OEB2BASE_X + add) - Base.half_of_base_length;
        double ce_base_x2 = (OEB2BASE_X + add) + Base.half_of_base_length;
        double ce_base_x3 = ce_base_x2;
        double ce_base_x4 = ce_base_x1;
        double ce_base_y1 = OEB2BASE_Y - Base.half_of_base_width;
        double ce_base_y2 = ce_base_y1;
        double ce_base_y3 = OEB2BASE_Y + Base.half_of_base_width;
        double ce_base_y4 = ce_base_y3;

        String OEB2_WITH_BASE = "" + "t{41 mc m0.2 xy(" + (nf.format(OEB2X + add)) + " " + nf.format(OEB2Y) + ")" + " " + "'OEB2'}" + "\n" +
                "b{41 xy(" + nf.format(ce_base_x1) + " " + nf.format(ce_base_y1) + " " + nf.format(ce_base_x2) + " " +
                nf.format(ce_base_y2) + " " + nf.format(ce_base_x3) + " " + nf.format(ce_base_y3) + " " + nf.format(ce_base_x4) + " " +
                nf.format(ce_base_y4) + ")}" + "\n";
        return OEB2_WITH_BASE;
    }


}

class Base {
    public static double half_of_base_length = 0.08;
    public static double half_of_base_width = 0.04;
}

class LeftRightARectangle {
    private String nbew="fddfdfdfd";////////////////////////////////////////////////////////////////////////
    public static double half_of_a_rect_length = 0.3505;
    public static double half_of_a_rect_width = 0.168;
}

class IO_Sizes {
    public static final double I_half_of_length = 0.078;
    public static final double I_half_of_width = 0.0425;
    public static final double O_half_of_length = 0.0915;
    public static final double O_half_of_width = 0.0425;
}

class IO_Base_Size {
    public static final double BASE_half_of_length = 0.08;
    public static final double BASE_half_of_width = 0.0665;
}

class IO_BASE_POINTS {
    public static final double i1minx = 14.759;
    public static final double o1minx = 15.010;
    public static final double i1maxx = 15.351;
    public static final double o1maxx = 15.602;
    public static final double i2minx = 16.757;
    public static final double o2minx = 17.008;
    public static final double i2maxx = 17.349;
    public static final double o2maxx = 17.600;
    public static final double common_y = 0.066;

}
// new branch add
class VssVdd {
    private Map<String, String> vssAndVdd;


    public VssVdd() {
        vssAndVdd = new HashMap<>();
        vssAndVdd.put("even", "t{42 mc fx m0.2 a180 xy(0.452 17.33) 'vss'}\n" +
                "b{42 xy(0.398 17.207 0.505 17.207 0.505 17.454 0.398 17.454)}\n" +
                "t{42 mc fx m0.2 a180 xy(0.765 17.308) 'vdd'}\n" +
                "b{42 xy(0.728 17.208 0.801 17.208 0.801 17.408 0.728 17.408)}\n");
        vssAndVdd.put("odd", "t{42 mc m0.2 xy(0.75 13.216) 'vdd'}\n" +
                "b{42 xy(0.734 13.16 0.767 13.16 0.767 13.273 0.734 13.273)}\n" +
                "t{42 mc m0.2 xy(0.442 13.776) 'vss'}\n" +
                "b{42 xy(0.425 13.699 0.46 13.699 0.46 13.854 0.425 13.854)}\n");
    }














    public String getVssAndVdd(String name) throws IllegalAccessException {
        if (!(name.equalsIgnoreCase("even") || name.equalsIgnoreCase("odd")))
            throw new IllegalAccessException("wrong vss and vdd info");
        return vssAndVdd.get(name.toLowerCase());
    }


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
/*
private static final String dec8x256_right = "s{'sgd_dec_8x256_right' xy(17.783 12.929)}";
 */
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