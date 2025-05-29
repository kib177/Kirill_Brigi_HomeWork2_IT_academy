package by.HomeWork;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Шаг 1: Настройка кодировки и типа контента.
 * Шаг 2: Проверка наличия данных в rows.
 * Шаг 3: Построчная обработка
 * Шаг 4: Вывод ошибки при некорректных данных.
 * Шаг 5: Расчет и вывод результатов:
 * --------------------------------------------
 * Средняя цена: (double) allSum / quantSum.
 * Общая стоимость (allSum).
 * Общее количество (quantSum).
 */
@WebServlet(urlPatterns = "/manual_input")
public class ProcessManualInput extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        String[] rows = req.getParameterValues("rows");
        PrintWriter writer = resp.getWriter();

        writer.write("<p><span style='color: red; font-size: 22px;'>Калькулятор усреднения акций</span></p>");
        writer.write("<table border = '1' cellpadding = '3' width = '250'>");
        writer.write("<tr><td><b>Цена акций</b></td><td><b>Кол-во</b></td><td><b>Сумма</b></td></tr>");

        if (rows == null || rows.length == 0) {
            writer.write("<p>Нет данных для обработки 'rows' </p>");
            return;
        }

        long quantSum = 0;
        long allSum = 0;
        boolean hasError = false;
        String error = "";


        for (int i = 0; i < rows.length; i++) {
            String row = rows[i];
            //Разделение строки на части.
            String[] cells = row.split(",");

            //Проверка на количество элементов
            if (cells.length != 2) {
                hasError = true;
                error = "Ошибка в строке " + (i + 1) + ": требуется 2 значения";
                break;
            }

            try {
                //Парсинг чисел и проверка на отрицательные значения
                long price = Long.parseLong(cells[0].trim());
                long quantity = Long.parseLong(cells[1].trim());

                if (price < 0 || quantity < 0) {
                    hasError = true;
                    error = "Ошибка в строке " + (i + 1) + ": отрицательное число";
                    break;
                }

                //Расчет суммы по строке и добавление в общие итоги.
                long sum = price * quantity;

                // Проверка на переполнение
                if (price > 0 && quantity > Long.MAX_VALUE / price) {
                    hasError = true;
                    error = "Переполнение типа данных Long";
                    break;
                }

                quantSum += quantity;
                allSum += sum;

                // Добавление строки таблицы
                writer.write("<tr>");
                writer.write("<td>" + price + "</td>");
                writer.write("<td>" + quantity + "</td>");
                writer.write("<td>" + sum + "</td>");
                writer.write("</tr>");

            } catch (NumberFormatException e) {
                hasError = true;
                error = "Ошибка в строке " + (i + 1) + ": нечисловые данные";
                break;
            }
        }

        writer.write("</table>");

        if (hasError) {
            writer.write("<p style='color:red'>" + error + "</p>");
            return;
        }

        String medCost = quantSum > 0 ? String.format("%.2f", (double) allSum / quantSum) : "0.00";

        writer.write("<table border = '1' cellpadding = '3' width = '250'>");
        writer.write("<tr><td><b>Средняя цена:</b></td><td><b>" + medCost + "</b></td></tr>");
        writer.write("<tr><td><b>Общая сумма:</b></td><td><b>" + allSum + "</b></td></tr>");
        writer.write("<tr><td><b>Общее количество:</b></td><td><b>" + quantSum + "</b></td></tr>");
        writer.write("</table>");
    }
}

