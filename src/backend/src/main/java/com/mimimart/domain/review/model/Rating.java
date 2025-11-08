package com.mimimart.domain.review.model;

import com.mimimart.domain.review.exception.InvalidRatingException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 評分值對象
 * 確保評分必須在 1-5 範圍內（Domain 層第二道驗證）
 */
@Getter
@EqualsAndHashCode
public class Rating {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final int value;

    private Rating(int value) {
        if (value < MIN_RATING || value > MAX_RATING) {
            throw new InvalidRatingException(value);
        }
        this.value = value;
    }

    /**
     * 建立評分值對象
     *
     * @param value 評分值（1-5）
     * @return Rating 值對象
     * @throws InvalidRatingException 當評分不在 1-5 範圍內時
     */
    public static Rating of(int value) {
        return new Rating(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
