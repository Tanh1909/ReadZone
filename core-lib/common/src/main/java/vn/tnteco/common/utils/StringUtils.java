package vn.tnteco.common.utils;

import liqp.TemplateParser;
import lombok.experimental.UtilityClass;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import vn.tnteco.common.core.json.JsonObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.UUID;
import java.util.regex.Pattern;

@UtilityClass
public class StringUtils {
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    private static final String PHONE_REGEX = "(84|\\+84|0)(\\d{10}|\\d{9}|\\d{8})";
    public static final String EMAIL_REGEX = "([a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    public static String removeAccent(String s) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(s)) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("")
                .replaceAll("đ", "d")
                .replaceAll("Đ", "D")
                .replaceAll("`", "")
                .replaceAll("´", "")
                .replaceAll("\\^", "");
    }

    public static String textToUrl(String text) {
        if (text == null) return "";
        text = removeAccent(text);
        text = text.toLowerCase();
        text = text.replaceAll("[^a-zA-Z0-9 ]", "");
        text = text.replaceAll(" ", "-");
        return text;
    }

    public static String formatDouble(Double number) {
        if (number == null) {
            return "0";
        }

        // Tạo DecimalFormatSymbols để tùy chỉnh định dạng
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.'); // Dấu phân cách phần nghìn
        symbols.setDecimalSeparator(','); // Dấu phân cách phần thập phân

        // Tạo DecimalFormat với định dạng mong muốn
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.0", symbols);

        // Format số
        return decimalFormat.format(number);
    }

    //decode unicode to character
    public static String decodeUnicode(String s) {
        return StringEscapeUtils.unescapeJava(s);
    }

    /**
     * Return "" if html.size() > 4mb (jsoup cannot parse this text)
     *
     * @param html
     * @return
     */
    public static String html2text(String html) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(html) || html.length() > 1000000) {
            return "";
        }
        return Jsoup.parse(html).text();
    }

//    public static void main(String[] args) {
////        String s = "<div class=\"parse-text content-editable text\"><div><span contenteditable=\"false\"><span data-fancybox=\"image-581cbd24-8e47-4e56-8b7f-f3811e97ee7f\" data-caption=\"Đăng bởi <a href=&quot;/profile?id=99770087237485483&quot;>Nguyễn Thị Thu Hương</a>\" href=\"https://cdn.noron.vn/2022/07/20/49956195229058779-1658325469.jpg\"><img class=\"content-embed note-float-center e-content-image\" src=\"https://cdn.noron.vn/2022/07/20/49956195229058779-1658325469.jpg\"></span></span></div><div>Phép màu - tên cuốn sách khởi động buổi sáng hôm nay của tôi.</div><div>Phép màu - là những gì tôi cảm nhận được khi đặt bút xuống cuốn sổ để viết ra 10 điều khiến tôi cảm thấy hạnh phúc khi thức dậy vào sáng nay.</div><div>Phép màu - khi tôi đã hoàn thành xong chuỗi 6 bài viết Hành trình tự do - tự do tài chính tại Blog Phụ Nữ Tự Do với topic: Những hiểu lầm về tự do tài chính.</div><div>Phép màu - là điều tôi có được với cuốn sách mà tôi lỡ hẹn suốt 4 năm qua, Nhà Giả Kim. Hôm nay tôi đã bắt đầu đọc nó rồi đấy.</div><div>Phép màu là thứ mà ta có thể tự tạo ra, bằng một cách chẳng ai ngờ tới nhưng nó lại vô cùng đơn giản.</div><div>Bạn đã từng nhận ra phép màu có ở quanh mình không?</div><div>Thử nghĩ xem, năm đó làm sao bạn thi đỗ vào trường đại học? Có thể nó đã được đánh đổi bằng rất nhiều đêm tròn mắt ôn thi.</div><div>Thử nghĩ xem, năm đó làm sao bạn vượt qua vòng phỏng vấn? Có thể nó đã được đánh đổi bởi những lần phỏng vấn thất bại trước đó, hoặc những ngày xù đầu làm bài tập lớn, bài tập nhỏ trong trường đại học để có một bảng điểm đẹp. Có thế nó được đánh đổi bởi những ngày hè oi bức, chạy xe đến công ty cách nhà nửa thành phố để thực tập cho biết người biết ta.</div><div>Thử nghĩ xem, năm đó làm sao Blog Hương Nguyễn ra đời? Câu này thì để tôi tự trả lời, nó được ra đời bởi những ngày căm ghét đi làm, bị kìm kẹp trong khuôn khổ đầy mệt mỏi và bất mãn, khiến tôi muốn vùng vẫy và bước ra thế giới bên ngoài. Tôi chấp nhận \"thất nghiệp\" để theo đuổi cái nghiệp viết tưởng chứng chẳng dành cho tôi.</div><div>Thử nghĩ xem, ngày đó làm sao Podcast của Phụ Nữ Tự Do lên sóng? Câu này cũng là của tôi. Tôi đã dành hơn 5 ngày chỉ để thu tập Podcast đâu tiên. Hết sửa nội dung đến đổi tone giọng, gửi cho Mentor rồi lại chỉnh lại thu, chưa kể đến việc chọn sao cho một cái tên phù hợp với tinh thần của tập Podcast đầu tiên lên sóng. Cuối cùng, hôm nay Podcast cũng đã chạm cột mốc 1000 lượt nghe với 14 tập được lên sóng.</div><div>Tôi chưa từng mơ thấy mình sẽ thì đỗ Đại học Bách Khoa khi còn nhỏ.</div><div>Tôi chưa từng mơ thấy mình trở thành chuyên viên đào tạo khi ngồi trên ghế nhà trường.</div><div>Tôi chưa từng mơ thấy mình kiếm tiền từ việc viết lách tự do khi vẫn còn là một nhân viên lãnh lương tháng ổn định.</div><div>Tôi chưa từng mơ thấy mình sẽ thu một file âm thanh để chia sẻ thông tin đến nhiều người.</div><div>Tôi chưa từng làm mơ thấy điều đó. Nhưng tôi từng chọn điều đó trước khi bắt tay vào thực hiện. Đây là sự thật.</div><div>Tôi chọn Đại Học Bách Khoa Hà Nội là ngôi trường tôi sẽ tới học sau 12 năm đèn sách. Tôi chọn Chuyên viên Đào Tạo nội bộ là vị trí tôi sẽ làm việc tại một doanh nghiệp lớn. Tôi chọn có một Blog của riêng mình khi kết thúc công việc ổn định nhiều năm. Tôi chọn Blog Phụ Nữ Tự Do sẽ tiếp cận thêm độc giả mới thông qua thính giác của họ.</div><div>Tôi chọn và biến mục tiêu đó bằng phép màu.</div><div>Phép màu này được góp từ những hiểu lầm, những sai sót, những bài học, sự cố gắng, đôi chút mệt mỏi, một chút buông bỏ và nhiều lần kiên trì đeo bám mục tiêu.</div><div>Khi tôi viết những bài viết đầu tiên về quản lý tài chính cá nhân, tôi có chút lo lắng. Lo lắng bởi vì tôi chưa phải một chuyên gia quản lý tài chính cá nhân \"lão làng\". Tôi chỉ đơn thuần là một cô gái độc thân, theo đuổi sự tự do trong đời và muốn viết xuống trải nghiệm của chính mình.</div><div>Nhưng rồi, tôi vẫn cứ gõ xuống những hàng chữ thẳng tắp, chúng dần hiện trên màn hình laptop của tôi, từ từ đến gần hơn với mọi người.</div><div>Tôi không biết chính xác mình đã giúp đỡ được ai chưa? nhưng tôi biết tôi đang giúp đỡ chính mình.</div><div>Tôi nghiêm túc với những gì chia sẻ, tôi thử nghiệm trước tất cả những gì tôi viết ra và mang câu chuyện chân thật của mình đến với bạn đọc.</div><div>Tôi thấy mình dũng cảm đối mặt với chính mình ở rất nhiều khía cạnh. Tôi bắt đầu đọc nhiều sách về tài chính cá nhân, học các khóa học đầu tư, đưa tiền của mình vào thị trường chứng khoán, tự rút ra bài học từ những phương pháp quản lý tài chính cá nhân học được qua nhiều năm chật vất với chúng, tôi tự mình viết xuống những sai lầm trong quản lý tiền bạc của mình, tìm ra nguyên nhân thật sự đứng phía sau nó, tìm kiếm giải pháp để tránh xa chúng... Tôi đã làm mọi thứ có thể và đồng thời chia sẻ mọi thứ tôi có được, vấp phải và đúc rút ra...</div><div>Tôi đã không còn rụt rè chia sẻ những bài viết của mình như những ngày đầu viết nữa. Bởi mỗi ngày tôi đều tiến bộ, trưởng thành và hiểu biết hơn.&nbsp;</div><div>Nhớ lại ngày trước, khi tôi mới bắt đầu rèn thói quen đọc sách, để nhờ lâu hơn cũng là giúp mình có nhiều trải nghiệm hơn với sách, tôi đã viết lại review cuốn sách đã đọc. Tôi đăng lên facebook nhưng chẳng dám để chế độ công khai, bởi tôi sợ sự non nớt của mình bị đánh giá.</div><div>Thế rồi, tôi nhận ra rằng, nếu tôi không tự đẩy mình ra ánh mắt trời thì tôi mãi luôn sợ ánh sáng.&nbsp;</div><div>Sự thật là, nếu đang đứng trong bóng tối, đột ngột đẩy ra ánh sáng, bạn sẽ tự nhiên cảm thấy vô cùng khó chịu với tần suất ánh sáng kia. Bạn có để ý rằng, khi bạn đột ngột tiếp xúc với ánh sáng cường độ cao, đôi mắt của bạn sẽ vô thức nheo lại cho đến khi thích nghi hẳn với ánh sáng ấy không?</div><div>Chính bởi điều đó, nên bạn đừng lo lắng khi chưa bước ra ngoài nắng, bạn sẽ tự nhiên học được cách thích nghi với nó thôi. Cứ cho mình cơ hội được va chạm trước đã.</div><div>Đến nay, khi nhắc tới sách, bạn bè sẽ hỏi tôi từng đọc cuốn này cuốn kia chưa? có cuốn sách nào chủ đề này, chủ đề kia giới thiệu không? muốn tìm sách viết về điều gì đó thì có cuốn nào hay không?</div><div>Đây là một thành công trong việc xây dựng thương hiệu cá nhân đó. Chẳng cần đánh bóng, chẳng cần tỏ ra mình là chuyên gia. Việc của tôi chỉ đơn giản là viết những gì tôi có, tôi thấy, tôi cảm, tôi học, tôi sai, tôi ngã...</div><div>Đơn giản chỉ có vậy.</div><div><span contenteditable=\"false\"><span data-fancybox=\"image-581cbd24-8e47-4e56-8b7f-f3811e97ee7f\" data-caption=\"Đăng bởi <a href=&quot;/profile?id=99770087237485483&quot;>Nguyễn Thị Thu Hương</a>\" href=\"https://cdn.noron.vn/2022/07/20/49956195229058780-1658325527.jpg\"><img class=\"content-embed note-float-center e-content-image\" src=\"https://cdn.noron.vn/2022/07/20/49956195229058780-1658325527.jpg\"></span></span></div><div>Tôi thường nghĩ rằng chỉ khi mình giỏi rồi mình mới chia sẻ được, chỉ khi mình thành công rồi mình mới nói đúng được. Thật ra đó là suy nghĩ của một đứa trẻ chưa \"trải sự đời\" của tôi. Ai cũng cần học tập mỗi ngày.</div><div>Chúng ta không phải thánh nhân biết tuốt. Có những thứ tôi biết nhưng bạn không biết, có những thứ bạn biết nhưng tôi không biết, có những thứ chúng ta cùng biết và có cả những thứ chúng ta cùng không biết.</div><div>Tôi chia sẻ thứ tôi biết, học tập thứ tôi không biết từ bạn. Chúng ta cùng tiến bộ.</div><div>Bạn chia sẻ thứ bạn biết, học tập thứ bạn chưa biết từ tôi. Chúng ta cùng tiến bộ.</div><div>Tôi chia sẻ thứ tôi và bạn đều biết, bạn chia sẻ thứ tôi và bạn đều biết. Chúng ta hoặc sẽ cùng cố thêm điều ta biết, hoặc sẽ khai mở một góc nhìn mới điều ta biết cho nhau. Chúng ta cùng tiến bộ.</div><div>Tôi chia sẻ thứ tôi chưa biết, bạn chia sẻ thứ bạn chư biết. Chúng ta sẽ cùng khám phá. Chúng ta cùng tiến bộ.</div><div>Chỉ đơn giản vậy thôi.</div><div>Nó giống như một câu nói mà tôi thường dùng để mở đầu các buổi thảo luận khi đứng vai trò là MC hoặc là người hướng dẫn.</div><div>\"Nếu tôi có một quả táo, bạn có một quả táo, chúng ta trao đổi cho nhau, thì mỗi chúng ta sẽ có một quả táo. Nếu tôi có một kiến thức mới, bạn có một kiến thức mới, chúng ta trao đổi cho nhau, thì mỗi chúng ta sẽ có hai kiến thức mới.\"</div><div>Cứ cởi mở, cứ tích cực, cứ học tập, cứ lao động... phép màu sẽ tới với tất cả chúng ta.</div><div>Hương Nguyễn - Phụ Nữ Tự Do.</div><p>&nbsp;</p><p>&nbsp;</p></div>";
////        List<String> temp = Jsoup.parse(s).body().childNodes().get(0).childNodes()
////                .stream().map(Node::toString).collect(Collectors.toList());
////        System.out.println(temp);
//
//        JsonObject jsonObject = new JsonObject()
//                .put("currentValue", 3)
//                .put("maxThreshold", 2);
//
//        String template = """
//                {% if currentValue > maxThreshold %}
//                             ok
//                {% endif %}
//                """;
//        String render = TemplateParser.DEFAULT.parse(template).render(jsonObject.getMap());
//        if ("ok".equals(render.trim())) {
//            System.out.println("oke");
//        }
//    }

    public static String cleanContent(String content) {
        return html2text(content)
                .replaceAll("\\\\", "")
                .replaceAll("\"", "")
                .replaceAll("'", "")
                .replaceAll("\n", " ");
    }

    public static String generateTextByLiquid(String template, JsonObject param) {
        return TemplateParser.DEFAULT.parse(template).render(param.getMap());
    }

    public static String toSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public static String snakeCaseToCamelCase(String input) {
        StringBuilder camelCase = new StringBuilder();
        for (String word : input.split("_")) {
            camelCase.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }
        return camelCase.substring(0, 1).toLowerCase() + camelCase.substring(1);
    }

    public static String snakeCaseToCamelCaseUpperFirst(String input) {
        StringBuilder camelCase = new StringBuilder();
        for (String word : input.split("_")) {
            camelCase.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }
        return camelCase.toString();
    }

    public static String removeAccentToLowerCase(String s) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(s)) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("")
                .replaceAll("đ", "d")
                .replaceAll("Đ", "D")
                .replaceAll("`", "")
                .replaceAll("´", "")
                .replaceAll("\\^", "")
                .trim()
                .toLowerCase();
    }

    public static boolean isValidEmail(String email) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(email)) return false;
        return email.matches(EMAIL_REGEX);
    }

    /**
     * Valid phone number start with 0 or +84 or 84
     * - 9 -10 number
     *
     * @param phoneNumber
     * @return
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(phoneNumber)) return false;
        return phoneNumber.matches(PHONE_REGEX);
    }

    public static String capitalize(String str) {
        return org.apache.commons.lang3.StringUtils.capitalize(str);
    }

    public static String genCorrelationId(String appName) {
        return genCorrelationId(null, appName);
    }

    public static String genCorrelationId(byte[] correlationIdByte, String appName) {
        if (correlationIdByte != null) {
            return new String(correlationIdByte);
        }
        String uuId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        return (appName + "-" + uuId).trim();
    }

}
