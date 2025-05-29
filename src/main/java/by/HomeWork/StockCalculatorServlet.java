package by.HomeWork;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
  Назначение:
  Сервлет обрабатывает POST-запросы по адресу /calculating_cost, вычисляя стоимость акций на основе данных, введенных пользователем.
  Результаты отображаются в виде HTML-таблицы с детализацией по каждой позиции и итоговой сводкой.
 */
@WebServlet("/calculating_cost")
public class StockCalculatorServlet extends HttpServlet {

    // HTML_HEAD - начальная часть HTML-страницы с CSS-стилями (красный заголовок, оформление таблиц)
    private static final String HTML_HEAD = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>Калькулятор акций</title>
            <style>
                .header { color: red; font-size: 22px; }
                table { border-collapse: collapse; width: 300px; margin-bottom: 20px; }
                td, th { border: 1px solid #ddd; padding: 8px; text-align: left; }
                th { background-color: #f2f2f2; }
                .error { color: red; }
            </style>
        </head>
        <body>
        """;

    //HTML_TAIL - закрывающие теги страницы.
    private static final String HTML_TAIL = "</body></html>";

    /**
      Хранит результаты обработки:
      totalQuantity - общее количество акций.
      totalSum - общая стоимость.
      hasErrors - флаг наличия ошибок в данных.
     */
    private static class Result {
        long totalQuantity = 0;
        long totalSum = 0;
        boolean hasErrors = false;
    }

    /**
      Устанавливает кодировку UTF-8.
      Извлекает параметры: num1 (количество акций), num2 (цена за акцию), currency (валюта).
      Проверяет валидность входных данных.
      Формирует HTML-ответ с результатами.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        StringBuilder htmlBuilder = new StringBuilder(HTML_HEAD);
        htmlBuilder.append("<p class='header'>Результат стоимости акций</p>");

        // Извлечение параметров
        String[] amounts = req.getParameterValues("num1");
        String[] costs = req.getParameterValues("num2");
        String currency = req.getParameter("currency");

        // Основная обработка
        if (input(amounts, costs)) {
            htmlBuilder.append("<p class='error'>Ошибка: неверные входные данные</p>")
                    .append(HTML_TAIL);
            resp.getWriter().write(htmlBuilder.toString());
            return;
        }

        Result result = processData(amounts, costs, currency, htmlBuilder);
        build(result, currency, htmlBuilder);

        htmlBuilder.append(HTML_TAIL);
        resp.getWriter().write(htmlBuilder.toString());
    }

    // Проверяет массивы amounts и costs на null, пустоту и несоответствие длин.
    private boolean input(String[] amounts, String[] costs) {
        return amounts == null || costs == null ||
                amounts.length == 0 || costs.length == 0 ||
                amounts.length != costs.length;
    }

    /**
      Обрабатывает каждую пару значений (количество/цена).
      Генерирует строки HTML-таблицы с детализацией расчетов.
      Возвращает ProcessingResult.
     */
    private Result processData(String[] amounts, String[] costs,
                               String currency, StringBuilder htmlBuilder) {

        Result result = new Result();
        htmlBuilder.append("<table><tr><th>Цена</th><th>Кол-во</th><th>Сумма</th></tr>");

        int rows = Math.min(amounts.length, costs.length);
        for (int i = 0; i < rows; i++) {
            processRow(amounts[i], costs[i], currency, i, htmlBuilder, result);
        }

        htmlBuilder.append("</table>");
        return result;
    }

    /**
      Анализирует данные одной строки:
      Пропускает полностью пустые строки.
      Фиксирует ошибки при частичном заполнении или нечисловых значениях.
      Рассчитывает сумму (цена × количество).
      Обновляет итоговые значения.
     */
    private void processRow(String amountStr, String costStr, String currency,
                            int rowIndex, StringBuilder htmlBuilder, Result result) {

        // Пропуск пустых строк
        if ((amountStr == null || amountStr.isBlank()) &&
                (costStr == null || costStr.isBlank())) {
            return;
        }

        // Проверка на частично заполненные строки
        if (amountStr == null || amountStr.isBlank() ||
                costStr == null || costStr.isBlank()) {

            htmlBuilder.append("<tr><td colspan='3' class='error'>")
                    .append("Ошибка: неполные данные в строке ")
                    .append(rowIndex + 1)
                    .append("</td></tr>");
            result.hasErrors = true;
            return;
        }

        try {
            // расчет значений
            long quantity = Long.parseLong(amountStr.trim());
            long price = Long.parseLong(costStr.trim());
            long sum = price * quantity;

            // Добавление строки в таблицу
            htmlBuilder.append("<tr><td>").append(price).append(" ").append(currency).append("</td>")
                    .append("<td>").append(quantity).append(" шт.</td>")
                    .append("<td>").append(sum).append(" ").append(currency).append("</td></tr>");

            result.totalQuantity += quantity;
            result.totalSum += sum;

        } catch (NumberFormatException e) {
            htmlBuilder.append("<tr><td colspan='3' class='error'>")
                    .append("Ошибка формата в строке ").append(rowIndex + 1)
                    .append(": ").append(amountStr).append(" / ").append(costStr)
                    .append("</td></tr>");
            result.hasErrors = true;
        }
    }

    /**
     Формирует сводную таблицу с:
     Средней ценой (с округлением до 2 знаков).
     Общей суммой.
     Общим количеством акций.
     Учитывает наличие ошибок в данных.
     */

    private void build(Result result, String currency,
                       StringBuilder htmlBuilder) {

        if (result.hasErrors) {
            htmlBuilder.append("<p class='error'>Обнаружены ошибки в данных. Результаты могут быть неполными.</p>");
            return;
        }

        if (result.totalQuantity == 0) {
            htmlBuilder.append("<p>Нет данных для расчета</p>");
            return;
        }

        double avgPrice = (double) result.totalSum / result.totalQuantity;

        htmlBuilder.append("<table>")
                .append("<tr><td><b>Средняя цена:</b></td><td><b>")
                .append(String.format("%.2f", avgPrice)).append(" ").append(currency).append("</b></td></tr>")
                .append("<tr><td><b>Общая сумма:</b></td><td><b>")
                .append(result.totalSum).append(" ").append(currency).append("</b></td></tr>")
                .append("<tr><td><b>Общее количество:</b></td><td><b>")
                .append(result.totalQuantity).append(" шт.</b></td></tr>")
                .append("</table>");
    }
}
