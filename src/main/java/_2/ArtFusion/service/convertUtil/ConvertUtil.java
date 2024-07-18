package _2.ArtFusion.service.convertUtil;

import _2.ArtFusion.controller.generateStoryController.storyForm.GenerateTemporaryForm;
import _2.ArtFusion.domain.Character.Gender;
import _2.ArtFusion.domain.r2dbcVersion.Characters;
import _2.ArtFusion.domain.r2dbcVersion.StoryBoard;
import _2.ArtFusion.domain.storyboard.GenerateType;
import _2.ArtFusion.domain.storyboard.Style;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static _2.ArtFusion.controller.generateStoryController.storyForm.GenerateTemporaryForm.*;

@Slf4j
public class ConvertUtil {

    /**
     * storyBoard build
     * @param form
     * @return
     */
    public static _2.ArtFusion.domain.r2dbcVersion.StoryBoard convertStoryBoard(GenerateTemporaryForm form, Long userId) {
        log.info("convertStoryBoard start");
        return _2.ArtFusion.domain.r2dbcVersion.StoryBoard.builder()
                .title(form.getTitle())
                .promptKor(form.getPromptKor())
                .style(convertToStyleTypeEnum(form.getStyle()))
                .generateType(checkToGenerateTypeEnum(form.getGenerateType()))
                .genre(convertToGenre(form.getGenre()))
                .wishCutCount(form.getWishCutCnt())
                .userId(userId)
                .build();
    }

    /**
     * Characters build
     * @param form
     * @return
     */
    public static List<_2.ArtFusion.domain.r2dbcVersion.Characters> convertCharacter(List<CharacterForm> form, StoryBoard storyBoard) {
        List<_2.ArtFusion.domain.r2dbcVersion.Characters> characters = new ArrayList<>();
        for (CharacterForm characterForm : form) {
            log.info("Character build={}",characterForm.getName());
            characters.add(Characters.builder()
                    .characterPrompt(characterForm.getCharacterPrompt())
                    .gender(convertToGenderEnum(characterForm.getGender()))
                    .name(characterForm.getName())
                    .storyId(storyBoard.getId())
                    .build());
        }
        return characters;
    }

    /**
     * GenreForms -> Genre 변환
     * @param genreForms
     * @return
     */
    public static String convertToGenre(List<String> genreForms) {
        return String.join(",", genreForms);
    }

    /**
     * GenerateTypeForm -> GenerateType
     * @param generateType
     * @return
     */
    public static String checkToGenerateTypeEnum(String generateType) {
        try {
            // GenerateType이 유효한 enum 값인지 확인합니다.
            GenerateType.valueOf(generateType);
            // 유효하면 그대로 반환합니다.
            return generateType;
        } catch (IllegalArgumentException e) {
            // 유효하지 않으면 예외를 발생시킵니다.
            throw new IllegalArgumentException("Invalid generateType: " + generateType);
        }
    }

    public static Style convertToStyleTypeEnum(String generateType) {
        return Style.valueOf(generateType);
    }

    /**
     * GenderForm -> Gender
     *
     * @param gender
     * @return
     */
    public static Gender convertToGenderEnum(GenderForm gender) {
        return Gender.valueOf(gender.name());
    }
}
