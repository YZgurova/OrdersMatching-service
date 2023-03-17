DELIMITER //
CREATE PROCEDURE save_exceeding_shares(ticker_temp VARCHAR(50), quantity_temp INT, price_temp DECIMAL(19, 2), status_temp VARCHAR(15))
BEGIN
    START TRANSACTION;
    SET @temp_quantity = (SELECT quantity FROM exceeding_shares WHERE ticker = ticker_temp AND status = status_temp);
    IF @temp_quantity IS NULL THEN
        INSERT INTO exceeding_shares (ticker, quantity, price) VALUES (ticker_temp, quantity_temp, price_temp);
    ELSE
        UPDATE exceeding_shares
        SET quantity = @temp_quantity + quantity_temp,
            price = price_temp
        WHERE ticker = ticker_temp
          AND status = status_temp;
    END IF;
    COMMIT;
END//
DELIMITER ;