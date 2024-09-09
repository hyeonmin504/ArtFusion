package _2.ArtFusion.service.util.convertUtil;

import _2.ArtFusion.controller.generateStoryApiController.storyForm.GenerateTemporaryForm;
import _2.ArtFusion.domain.actor.Gender;
import _2.ArtFusion.domain.r2dbcVersion.Actor;
import _2.ArtFusion.domain.r2dbcVersion.StoryBoard;
import _2.ArtFusion.domain.storyboard.GenerateType;
import _2.ArtFusion.domain.storyboard.Style;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static _2.ArtFusion.controller.generateStoryApiController.storyForm.GenerateTemporaryForm.*;

@Slf4j
public class ConvertUtil {

    /**
     * storyBoard build
     * @param form
     * @return
     */
    public static StoryBoard convertStoryBoard(GenerateTemporaryForm form, Long userId) {
        log.info("convertStoryBoard start");

        String prompt = form.getPromptKor()
                .replace(".\n\n", ". ")
                .replace("\n\n", ". ")
                .replace(".\n", ". ")
                .replace("\n", ". ");
        log.info("convert/form.getPrompt={}",prompt);

        return StoryBoard.builder()
                .title(form.getTitle())
                .promptKor(prompt)
                .style(convertToStyleTypeEnum(form.getStyle()))
                .generateType(checkToGenerateTypeEnum(form.getGenerateType()))
                .genre(convertToGenre(form.getGenre()))
                .cutCnt(form.getWishCutCnt())
                .userId(userId)
                .build();
    }

    /**
     * Characters build
     * @param form
     * @return
     */
    public static List<Actor> convertCharacter(List<CharacterForm> form, StoryBoard storyBoard) {
        List<Actor> characters = new ArrayList<>();
        for (CharacterForm characterForm : form) {
            log.info("Character build={}",characterForm.getName());
            characters.add(Actor.builder()
                    .characterPrompt(characterForm.getCharacterPrompt())
                    //.gender(convertToGenderEnum(characterForm.getGender()))
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
