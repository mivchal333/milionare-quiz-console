package service;

import model.Prize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PrizesService {
    private final List<Prize> prizesModel;

    public PrizesService() {
        List<Prize> prizesModel = new ArrayList<>();
        prizesModel.add(new Prize(0, false));
        prizesModel.add(new Prize(1_000, true));
        prizesModel.add(new Prize(2_000, false));
        prizesModel.add(new Prize(5_000, false));
        prizesModel.add(new Prize(10_000, false));
        prizesModel.add(new Prize(20_000, false));
        prizesModel.add(new Prize(40_000, true));
        prizesModel.add(new Prize(75_000, false));
        prizesModel.add(new Prize(125_000, false));
        prizesModel.add(new Prize(250_000, false));
        prizesModel.add(new Prize(500_000, false));
        prizesModel.add(new Prize(1_000_000, true));
        this.prizesModel = prizesModel;
    }

    public int getObtainedPrize(int questionIndex) {
        List<Prize> prizesInRange = prizesModel.subList(0, questionIndex + 1);
        prizesInRange.sort(Comparator.comparingInt(Prize::getValue).reversed());

        Optional<Prize> prize = prizesInRange.stream()
                .filter(Prize::isGuaranteed)
                .findFirst();

        return prize.map(Prize::getValue).orElse(0);

    }

    public Prize getPrize(int questionIndex) {
        return prizesModel.get(questionIndex);
    }
}