package com.example.mr_hotdog_telegram_bot.service;

import com.example.mr_hotdog_telegram_bot.config.UserBotConfig;
import com.example.mr_hotdog_telegram_bot.order.OrderRepository;
import com.example.mr_hotdog_telegram_bot.order.OrderService;
import com.example.mr_hotdog_telegram_bot.order.entity.Order;
import com.example.mr_hotdog_telegram_bot.product.ProductService;
import com.example.mr_hotdog_telegram_bot.product.entity.Product;
import com.example.mr_hotdog_telegram_bot.product.entity.ProductType;
import com.example.mr_hotdog_telegram_bot.user.UserRepository;
import com.example.mr_hotdog_telegram_bot.user.UserService;
import com.example.mr_hotdog_telegram_bot.user.entity.PayType;
import com.example.mr_hotdog_telegram_bot.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserTelegramBot extends TelegramLongPollingBot {
    //todo locationi togirlash
    //todo addcartda message chiqishi kere va uni sotib olish buyurtmalarim bolimidi bolishi kere
    //not work addCart faqat oxirgisi qowilvotti
    //not work buyurtmalarni tozalash
    private final UserBotConfig botConfig;
    private final ProductService productService;
    private final UserService userService;
    private final UserRepository userRepository;

    private final OrderRepository userOrder;
    private final OrderService orderService;

    private final String momChatId = "76676513";
    private final String meChatId = "842230958";


    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            if (update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                switch (text) {
                    case "/start" -> {
                        SendMessage start = sendPhoneNumber(chatId);
                        execute(start);
                    }
                }
            }
            if (update.getMessage().hasPhoto()) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("Buyurtmangiz qabul qilindi siz bilan bog'lanamiz");
                userPictureSendToAdmin(update.getMessage().getPhoto(), chatId);

            }
            if (update.getMessage().hasLocation()){

            }
            if (update.getMessage().hasContact()) {

                Contact contact = update.getMessage().getContact();

                User user = User.builder()
                        .phoneNumber(contact.getPhoneNumber())
                        .name(contact.getFirstName())
                        .chatId(update.getMessage().getChatId())
                        .build();

                userService.create(user);

                SendMessage order = orderType(chatId, update);
                execute(order);
            }

            if (update.getMessage().hasLocation()) {

                Location location = update.getMessage().getLocation();

                Contact contact = update.getMessage().getContact();

                userService.update(User.builder()
                        .orderType("delivery")
                        .location(location)
                        .build(), chatId);
                SendMessage menu = menu(update.getMessage().getChatId(), update, null);

                execute(menu);
            }

        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            SendMessage sendMessage = takeOrDelivery(data, update);
            execute(sendMessage);

        }
    }

    private SendMessage takeOrDelivery(String data, Update update) {

        SendMessage sendMessage = new SendMessage();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        Message message = update.getCallbackQuery().getMessage();


        String productId = null;
        if (data.contains(":")) {
            String[] dataArray = data.split(":");
            if (dataArray.length == 2) {
                String data1 = dataArray[0];
                String data2 = dataArray[1];
                data = data1;
                productId = data2;

            }
        }

        switch (data) {
            case "begin", "end", "error" -> messageFromAdminToUser(data, Long.valueOf(productId));
            case "take_away" -> {
                deletePreviousMessage(update);
                userService.update( User.builder().orderType("take_away").build(), chatId);
                sendMessage = menu(chatId, update, data);
                break;
            }
            case "back" -> sendMessage = menu(chatId, update, data);
            case "delivery" -> {
                sendMessage = sendLocationButton(chatId);
                break;
            }
            case "userOrders" -> sendMessage = userOrders(chatId, update);
            case "menu", "backMenu", "backToProductType" -> {
                if (data.equals("backToProductType")) {
                    productService.updateProductCount(1, Integer.valueOf(Objects.requireNonNull(productId)));
                }
                sendMessage = allProduct(chatId, update);
                break;
            }
            case "burger" -> sendMessage = getProductByType(chatId, ProductType.BURGER, update);
            case "hotdog" -> sendMessage = getProductByType(chatId, ProductType.HOTDOG, update);
            case "cake" -> sendMessage = getProductByType(chatId, ProductType.CAKE, update);
            case "tea" -> sendMessage = getProductByType(chatId, ProductType.TEA, update);
            case "addCard" -> {

                sendMessage = addCard(chatId, productId, update);
                productService.updateProductCount(1, Integer.valueOf(Objects.requireNonNull(productId)));
                break;
            }
            case "deleteOrders" -> {
                orderService.deleteUserOrders((int) chatId);
                sendMessage = menu(chatId, update, data);
                break;
            }
            case "predication" -> {
                sendMessage =  predication(chatId, update);
                break;
            }
            case "PAY_WITH_CART" -> {
                sendMessage = payWithCart(chatId, update);
                break;
            }
            case "PAY_CASH" -> {
                sendMessage = payCash(chatId,update);
                break;
            }
            default -> {
                if (!(data.equals("+") || data.equals("-") || data.equals("counter"))) {
                    sendMessage = plusMinusOrder(data, chatId, update, productId);
                }
            }
        }

        return sendMessage;
    }

    private void messageFromAdminToUser(String data, long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        User user = userService.findByChatId(userId);
        if (user != null) {
            switch (data) {
                case "begin" -> {

                    orderService.deleteUserOrders((int) userId);
                    sendMessage.setText("Sizning Buyurtmangiz qabul qilindi");
                    break;
                }
                case "end" -> {
                    if ("delivery".equals(user.getOrderType())) {
                        sendMessage.setText("Sizning buyurtmangzi tayyor bo'ldi tez orada yetkazib beramiz");
                    } else {
                        sendMessage.setText("Sizning buyurtamangiz tayyor bo'ldi kelib olib ketishingiz mumkun");
                    }
                    break;
                }
                case "error" -> sendMessage.setText("sizning buyurtmangiz qabul qilinmadi ! \n Siz bilan bo'g'lanamiz");
            }

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException("Telegram xatoligi", e);
            }
        }
    }


    private void sendMessageToAdminTypeTakeAway(long userChatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(meChatId);

        List<Order> order = orderService.findAllOrder((int) userChatId);

        User user = userService.findByChatId(userChatId);
        double num = 0;

        StringBuilder messageText = new StringBuilder();
        if (!(order == null || order.isEmpty()) &&
                user.getOrderType().equals("take_away")) {

            for (Order product : order) {
                num += product.getPrise() * product.getCount();
                messageText.append("Userni telefon raqami:")
                        .append(user.getPhoneNumber())
                        .append("\n\nUser Buyurtmasi: ")
                        .append(product.getName())
                        .append(" ")
                        .append(product.getCount())
                        .append(" ta")
                        .append("\n\n");

            }
            messageText.append("Umumiy summa:").append(num)
                    .append("To'lov turi :")
                    .append("naxt pul to'lash")
                    .append("\nuser o'zi kelib olib keteadi");

            sendMessage.setText(messageText.toString());

            List<InlineKeyboardButton> inlineButtons = createInline("Tayyorlanvotti", "begin:" + userChatId,
                    "Tayor boldi", "end:" + userChatId,"buyurtma qabul qilinmadi", "error:" + userChatId);




            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(List.of(inlineButtons));
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else {
            sendMessage.setText("siz haliham buyurtma bermadingiz");
            sendMessage.setChatId(userChatId);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public SendMessage payCash(long chatId, Update update) {
        deletePreviousMessage(update);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        User user = userService.findByChatId(chatId);
        user.setPayType(PayType.PAY_CASH);
        userService.update(user, chatId);


        List<Order> orders = orderService.findAllOrder((int) chatId);

        double num = 0;
        StringBuilder messageText = new StringBuilder();
        for (Order order : orders) {

            orderService.updatePayType(chatId,PayType.PAY_CASH);
            num += order.getPrise() * order.getCount();
            messageText.append("Sizning Buyurtmangiz ")
                    .append(order.getName())
                    .append(" ")
                    .append(order.getCount())
                    .append(" ta")
                    .append("\n\n");

        }

        messageText.append("Umumiy summa:").append(num);
        messageText.append("\n\n Siz naxt pul to'lashni tanladingiz siz bilan bo'glanamiz");
        sendMessageToAdminTypeTakeAway(chatId);
        sendMessage.setText(messageText.toString());
        return sendMessage;
    }

    private SendMessage payWithCart(long chatId, Update update) {
        deletePreviousMessage(update);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        List<Order> orders = orderService.findAllOrder((int) chatId);

        double num = 0;
        StringBuilder messageText = new StringBuilder();
        messageText.append("Sizning Buyurtmangiz ");
        for (Order order : orders) {
            orderService.updatePayType(chatId,PayType.PAY_WITH_CART);
            num += order.getPrise() * order.getCount();
            messageText.append(order.getName())
                    .append(" ")
                    .append(order.getCount())
                    .append(" ta")
                    .append("\n\n");

        }
        messageText.append("Umumiy summa:").append(num);
        messageText.append("\n\n8600 51616 8489498 313215 manashu  cartaga pul tolab bizga scren shotni  yuborishingizni so'raymiz ");
        sendMessage.setText(messageText.toString());
        return sendMessage;
    }

    private SendMessage predication(Long chatId, Update update) {
        deletePreviousMessage(update);
        SendMessage sendMessage = new SendMessage();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        sendMessage.setChatId(chatId);
        User user = userService.findByChatId(chatId);

        if (user.getOrderType().equals("delivery")) {
            sendMessage.setText("8600 51616 8489498 313215 manashu  cartaga pul tolab bizga scren shotni  yuboring va buyurtmangizni qabul qilamiz");
        } else {
            sendMessage.setText("O'zingizga qulayini tanlang");
            List<InlineKeyboardButton> inlineButtons = createInline("carta orqali pul tolash ", PayType.PAY_WITH_CART.name(), "naxt pul tolash", PayType.PAY_CASH.name());
            inlineKeyboardMarkup.setKeyboard(List.of(inlineButtons));
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        }

        return sendMessage;
    }


    public SendMessage addCard(long chatId, String productId, Update update) {
        SendMessage sendMessage = new SendMessage();
        Optional<Product> byId = productService.findById(String.valueOf(productId));

        if (byId.isPresent()) {
            Product product = byId.get();
            if (!product.getIsHave()){
                sendMessage.setText("bu maxsulot vaxtinchalik mavjud emas");
                return sendMessage;
            }
            orderService.addOrder(Order.builder()
                            .prise(product.getPrise())
                            .info(product.getInfo())
                            .count(product.getCount())
                            .name(product.getName())
                            .userChatId((int) chatId)
                            .productType(product.getProductType())
                    .build());

//            sendMessage = getProductByType(chatId, product.getProductType(), update);
            sendMessage=allProduct(chatId,update);
        }

        return sendMessage;
    }


    private SendMessage getProductByType(Long chatId, ProductType productType, Update update) {
//        Order order = userOrder.findById(chatId).get();

        SendMessage sendMessage = new SendMessage();

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> row = new ArrayList<>();

        sendMessage.setChatId(chatId);

        sendMessage = sendTelegraph(chatId);
        List<Product> allByProductType = productService.getAllByProductType(productType);

        if (allByProductType.isEmpty()) {
            sendMessage.setText("Hozircha bu maxsulatimizdan mavjud emas");
            return sendMessage;
        } else {
            deletePreviousMessage(update);
            for (int i = 0; i < allByProductType.size(); i += 2) {
                Product product = allByProductType.get(i);
                Product lastProduct = allByProductType.get(allByProductType.size() - 1);

                if (!Objects.equals(product.getId(), lastProduct.getId())) {
                    Product product1 = allByProductType.get(i + 1);
                    List<InlineKeyboardButton> inline = createInline(product.getName(), product.getId().toString(), product1.getName(), String.valueOf(product1.getId()));
                    row.add(inline);
                } else {
                    List<InlineKeyboardButton> inline = createInline(product.getName(), product.getId().toString());
                    row.add(inline);
                }
            }

//            if (userOrder.findOrderByUserChatId(chatId).get().getProducts().isEmpty()) {
//                List<InlineKeyboardButton> cart = createInline("🛒 Savatcha", "userOrders");
//                row.add(cart);
//            }
        }


        List<InlineKeyboardButton> inline = createInline("⬅️ Ortga", "backMenu");
        row.add(inline);


        inlineKeyboardMarkup.setKeyboard(row);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }

    private SendMessage plusMinusOrder(String productId, Long chatId, Update update, String data) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        Product product = productService.findById(productId).get();

        if (!product.getIsHave()){
            sendMessage.setText("bu maxsulotimiz vaqtinchalik mavjud emas");
                return sendMessage;
        }

        int count = 1;
        try {
            if (data.equals("+")) {
                deletePreviousMessage(update);
                count = product.getCount();
                product.setCount(count + 1);
                count = product.getCount();
                productService.updateProductCount(count, Integer.valueOf(productId));
            } else if (data.equals("-")) {

                count = product.getCount();
                product.setCount(count - 1);
                count = product.getCount();
                productService.updateProductCount(count, Integer.valueOf(productId));

            }
        } catch (NullPointerException e) {
            System.out.println(e);
        }


        if (!(productId.equals("+") || productId.equals("-") || productId.equals("counter"))) {

            Optional<Product> optionalProduct = productService.findById(productId);
            if (optionalProduct.isPresent()) {
                Product products = optionalProduct.get();
                sendMessage.setText("Name: " + products.getName() + "\nNarxi: " + products.getPrise() + " so'm" + "\nTavsif: " + products.getInfo());

                InlineKeyboardButton plus = new InlineKeyboardButton("+");
                plus.setCallbackData(products.getId() + ":+");

                InlineKeyboardButton minus = new InlineKeyboardButton("-");
                minus.setCallbackData(products.getId() + ":-");

                InlineKeyboardButton keyboardButton = new InlineKeyboardButton(String.valueOf(count));
                keyboardButton.setCallbackData("counter");

                List<InlineKeyboardButton> inline = createInline("🛒 Savatga qo'shish", "addCard:" + products.getId());

                List<InlineKeyboardButton> backToProductType = createInline("⬅️ Ortga", "backToProductType:" + product.getId());

                List<InlineKeyboardButton> plus1 = List.of(minus, keyboardButton, plus);


                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.setKeyboard(List.of(plus1, inline, backToProductType));

                sendMessage.setReplyMarkup(inlineKeyboardMarkup);

            }

        }


        return sendMessage;
    }


    private SendMessage allProduct(Long chatId, Update update) {
        deletePreviousMessage(update);
        SendMessage sendMessage = sendTelegraph(chatId);

        List<InlineKeyboardButton> inline = createInline("🍔 Burger", "burger",
                "🌭 Hotdog", "hotdog");

        List<InlineKeyboardButton> inline2 = createInline("🍰 Shirinliklar", "cake",
                "🥤 Ichimliklar", "tea");

        InlineKeyboardButton back = new InlineKeyboardButton("⬅️ Asosiy menuga o'tish");
        back.setCallbackData("back");


        List<InlineKeyboardButton> back2 = List.of(back);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(inline, inline2, back2));

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }

    @SneakyThrows
    private SendMessage userOrders(long chatId, Update update) {
        SendMessage sendMessage = new SendMessage();
//        List<Product> products = userOrder.findById(chatId).get().getProducts();
        List<Order> orders = orderService.findAllOrder((int) chatId);

        if (orders.isEmpty()) {
            sendMessage.setChatId(chatId);
            sendMessage.setText("Hamon buyurtma bermadingiz");
            return sendMessage;
        } else {
            deletePreviousMessage(update);
            double num = 0;
            sendMessage.setChatId(chatId);
            List<InlineKeyboardButton> inline = createInline("⬅️ Ortga", "menu", "🚖 Buyurtmani tasdiqlash", "predication");
            List<InlineKeyboardButton> inline1 = createInline("🗑️ Savatni bo'shatish", "deleteOrders");
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            StringBuilder messageText = new StringBuilder("Savatchada:\n\n");
            for (Order order : orders) {
                num += order.getPrise() * order.getCount();
                messageText.append(" ")
                        .append(order.getName())
                        .append(" ")
                        .append(order.getCount())
                        .append(" ta")
                        .append("\n\n");

            }

            messageText.append("Umumiy summa:").append(num);
            sendMessage.setText(messageText.toString());

            inlineKeyboardMarkup.setKeyboard(List.of(inline1, inline));
            sendMessage.setText(messageText.toString());
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        }

        return sendMessage;
    }


    private SendMessage sendTelegraph(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Kategoriyalardan birini tanlang" + "<a href=\"https://telegra.ph/asd-12-26-20\" hidden=\"hidden\" >.</a>\n");
        sendMessage.setParseMode(ParseMode.HTML);
        return sendMessage;
    }

    private SendMessage sendPhoneNumber(Long chatId) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Iltimos burutma berish uchun telefon raqamingizni yuboring");
        KeyboardButton button = new KeyboardButton("📞 Telefon raqamini yuborish");
        ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup();
        replyMarkup.setResizeKeyboard(true);
        replyMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        button.setRequestContact(true);
        row.add(button);
        keyboard.add(row);
        replyMarkup.setOneTimeKeyboard(true);
        replyMarkup.setKeyboard(keyboard);
        replyMarkup.setResizeKeyboard(true);
        sendMessage.setReplyMarkup(replyMarkup);

        return sendMessage;
    }


    private SendMessage sendLocationButton(long chatId) {
        KeyboardButton button = new KeyboardButton("📍 Location");

        ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup();
        replyMarkup.setOneTimeKeyboard(true);
        replyMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        button.setRequestLocation(true);
        row.add(button);
        keyboard.add(row);
        replyMarkup.setKeyboard(keyboard);
        replyMarkup.setResizeKeyboard(true);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Qayerga yetkazib berishimzni yuboring.\n \n Eslatma yetkazib berishimiz Yandex orqali.");
        sendMessage.setReplyMarkup(replyMarkup);
        sendMessage.setChatId(chatId);

        return sendMessage;
    }


    private SendMessage menu(Long chatId, Update update, String data) {
        deletePreviousMessage(update);
        WebAppInfo webAppInfo = new WebAppInfo();
        webAppInfo.setUrl("https://linktr.ee/mr_Hotdog");


        SendMessage sendMessage = sendTelegraph(chatId);

        InlineKeyboardButton orders = new InlineKeyboardButton("🛒 Buyurtma berish");
        orders.setCallbackData("menu");

        InlineKeyboardButton userOrders = new InlineKeyboardButton("Buyurtmlarim");
        userOrders.setCallbackData("userOrders");

        InlineKeyboardButton location = new InlineKeyboardButton("📍 Bizning manzil");
        location.setWebApp(webAppInfo);

        List<InlineKeyboardButton> orders1 = List.of(orders);
        List<InlineKeyboardButton> userOrders1 = List.of(userOrders, location);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(List.of(orders1, userOrders1));

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }

    private SendMessage orderType(Long chatId, Update update) {
        deletePreviousMessage(update);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Quydagilardan birini tanlang");
        InlineKeyboardButton button1 = new InlineKeyboardButton("Olib ketish");
        button1.setCallbackData("take_away");

        InlineKeyboardButton button2 = new InlineKeyboardButton("Yetkazib berish");
        button2.setCallbackData("delivery");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(button1);
        row1.add(button2);


        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row1);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }


    public List<InlineKeyboardButton> createInline(String name, String callbackData) {
        InlineKeyboardButton burger = new InlineKeyboardButton(name);
        burger.setCallbackData(callbackData);
        return List.of(burger);
    }

    public List<InlineKeyboardButton> createInline(String name1, String callbackData1, String name2, String callbackData2) {
        InlineKeyboardButton inline1 = new InlineKeyboardButton(name1);
        inline1.setCallbackData(callbackData1);


        InlineKeyboardButton inline2 = new InlineKeyboardButton(name2);
        inline2.setCallbackData(callbackData2);

        return List.of(inline1, inline2);
    }

    public List<InlineKeyboardButton> createInline(String name1, String callbackData1, String name2, String callbackData2, String name3, String callbackData3) {
        InlineKeyboardButton inline1 = new InlineKeyboardButton(name1);
        inline1.setCallbackData(callbackData1);


        InlineKeyboardButton inline2 = new InlineKeyboardButton(name2);
        inline2.setCallbackData(callbackData2);

        InlineKeyboardButton inline3 = new InlineKeyboardButton(name3);
        inline3.setCallbackData(callbackData3);

        return List.of(inline1, inline2, inline3);
    }

    private void deletePreviousMessage(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        if (callbackQuery != null) {
            Long chatId = callbackQuery.getMessage().getChatId();
            Integer messageId = callbackQuery.getMessage().getMessageId();

            DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), messageId);
            try {
                execute(deleteMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace(); // Handl,updatee exception according to your needs
            }
        }
    }

    @SneakyThrows
    private void sendPhoto(long chatId, String imageCaption, String imagePath) {
        File file = ResourceUtils.getFile("classpath:" + imagePath);
        InputFile inputFile = new InputFile(file);

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(inputFile);
        sendPhoto.setChatId(chatId);
        sendPhoto.setCaption(imageCaption);
        execute(sendPhoto);
    }

    private void userPictureSendToAdmin(List<PhotoSize> photos, long chatId) throws TelegramApiException {
        User user = userService.findByChatId(chatId);
        double num = 0;

        List<Order> orders = orderService.findAllOrder((int) chatId);

        StringBuilder messageText = new StringBuilder();
        messageText.append("Userni telefon raqami:")
                .append(user.getPhoneNumber())
                .append("\n\nUser Buyurtmasi: ");
        if (!(orders == null || orders.isEmpty())) {
            for (Order order : orders) {
                num += order.getPrise() * order.getCount();
                messageText
                        .append(order.getName())
                        .append(" ")
                        .append(order.getCount())
                        .append(" ta")
                        .append("\n\n");

            }
            messageText.append("Umumiy summa:").append(num)
                    .append("\n\nTo'lov turi :")
                    .append("Carta orqali");
            if (user.getOrderType().equals("take_away")) {
                messageText.append("\nuser o'zi kelib olib ketadi");
            } else {
                messageText.append("\nuserga yetkazib berish kerak");
            }

            String f_id = photos.stream()
                    .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
                    .findFirst()
                    .orElse(null).getFileId();
            int f_width = photos.stream()
                    .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
                    .findFirst()
                    .orElse(null).getWidth();
            int f_height = photos.stream()
                    .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
                    .findFirst()
                    .orElse(null).getHeight();
            SendPhoto msg = new SendPhoto();
            InputFile inputFile = new InputFile(f_id);

            msg.setChatId(meChatId);
            msg.setPhoto(inputFile);
            msg.setCaption(messageText.toString());

            List<InlineKeyboardButton> inlineButtons = createInline("Qabul qilish", "begin:" + chatId,
                    "Tayor boldi", "end:" + chatId,
                    "buyurtma qabul qilinmadi", "error:" + chatId);

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(List.of(inlineButtons));
            msg.setReplyMarkup(inlineKeyboardMarkup);

            execute(msg);
        } else {
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), "Siz haliham buyurtma bermadingiz");
            execute(sendMessage);
        }
    }




    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }
}

