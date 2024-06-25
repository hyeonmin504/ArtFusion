package _2.ArtFusion.service.convertUtil;

import _2.ArtFusion.controller.generateStoryController.storyForm.GenerateTemporaryForm;
import _2.ArtFusion.domain.Character.Characters;
import _2.ArtFusion.domain.Character.Gender;
import _2.ArtFusion.domain.storyboard.GenerateType;
import _2.ArtFusion.domain.storyboard.StoryBoard;
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
    public static StoryBoard convertStoryBoard(GenerateTemporaryForm form) {
        log.info("convertStoryBoard start");
        return StoryBoard.builder()
                .title(form.getTitle())
                .promptKor(form.getPromptKor())
                .style(convertToStyleTypeEnum(form.getStyle()))
                .generateType(convertToGenerateTypeEnum(form.getGenerateType()))
                .genre(convertToGenre(form.getGenre()))
                .wishCutCount(form.getWishCutCnt())
                .build();
    }

    /**
     * Characters build
     * @param form
     * @return
     */
    public static List<Characters> convertCharacter(List<CharacterForm> form, StoryBoard storyBoard) {
        List<Characters> characters = new ArrayList<>();
        for (CharacterForm characterForm : form) {
            log.info("Character build={}",characterForm.getName());
            characters.add(Characters.builder()
                    .characterPrompt(characterForm.getCharacterPrompt())
                    .gender(convertToGenderEnum(characterForm.getGender()))
                    .name(characterForm.getName())
                    .storyBoard(storyBoard)
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
    public static GenerateType convertToGenerateTypeEnum(String generateType) {
        return GenerateType.valueOf(generateType);
    }

    public static Style convertToStyleTypeEnum(String generateType) {
        return Style.valueOf(generateType);
    }

    /**
     * GenderForm -> Gender
     * @param gender
     * @return
     */
    public static Gender convertToGenderEnum(GenderForm gender) {
        return Gender.valueOf(gender.name());
    }
}
