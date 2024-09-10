"use client";

import MainSection from "@/components/common/MainSection";
import styles from "./simple.module.scss";
import clsx from "clsx";
import Button from "@/components/common/Button";
import ActorInput from "@/components/storys/ActorInput";
import Input from "@/components/common/Input";
import { useState } from "react";
import { FiPlusCircle } from "react-icons/fi";
import { useRouter } from "next/navigation";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export default function Page() {
  const [title, setTitle] = useState("");
  const [promptKor, setPromptKor] = useState("");
  const [actorState, setActorState] = useState([
    { id: 0, name: "", characterPrompt: "" },
  ]);

  const requestUrl = `${process.env.NEXT_PUBLIC_SERVER_URL}/story/temporary`;
  const router = useRouter();

  const queryClient = useQueryClient();
  const onGenerateMutation = useMutation({
    mutationFn: async (data: any) => {
      const response = await fetch(requestUrl, {
        method: "POST",
        body: JSON.stringify(data),
        headers: { "Content-Type": "application/json" },
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      return response.json();
    },
    onSuccess: (data, variables, context) => {
      queryClient.invalidateQueries({ queryKey: ["archives-my"] });
      router.push(`/temporarys/${data.data}`);
    },
  });

  //btns
  const [selectedGenre, setSelectedGenre] = useState([
    { name: "판타지", en: "fan", isActive: false },
    { name: "무협", en: "mu", isActive: false },
    { name: "게임", en: "game", isActive: false },
    { name: "로맨스", en: "loman", isActive: false },
    { name: "중세", en: "cath", isActive: false },
    { name: "공포", en: "horr", isActive: false },
  ]);
  const [selectedStyle, setSelectedStyle] = useState({
    name: "일본 애니메이션",
    en: "japan",
  });
  const stylesArr = [
    { name: "일본 애니메이션", en: "JPN_ANI" },
    { name: "한국 웹툰", en: "KOR_WEBTOON" },
    // { name: "미국 만화", en: "america" },
    // { name: "동화", en: "child" },
  ];
  const onAddActor = () => {
    const responseArr = [...actorState];
    responseArr.push({ id: responseArr.length, name: "", characterPrompt: "" });
    setActorState(responseArr);
  };

  const handleInputChange = (e, idx) => {
    const { name, value } = e.target;

    setActorState((prevFormData) => {
      console.log(prevFormData);

      const responsArr = [...prevFormData];
      responsArr[idx] = {
        ...responsArr[idx],
        [name]: value,
      };
      return responsArr;
    });
  };

  const renderActors = actorState.map((item, idx) => (
    <ActorInput
      key={item.id}
      itemId={item.id}
      onChange={handleInputChange}
      nameValue={item.name}
      dscrValue={item.characterPrompt}
    />
  ));
  const onClickGengre = (idx) => {
    const responseArr = [...selectedGenre];
    if (responseArr[idx].isActive) {
      responseArr[idx].isActive = false;
    } else {
      responseArr[idx].isActive = true;
    }
    setSelectedGenre(responseArr);
  };

  const renderGenre = selectedGenre.map((item, idx) => (
    <Button
      key={item.en}
      color="primary"
      type="border"
      isActive={item.isActive}
      onClick={() => {
        onClickGengre(idx);
      }}
    >
      {item.name}
    </Button>
  ));

  const renderStyle = stylesArr.map((item, idx) => (
    <Button
      key={item.en}
      color="primary"
      type="border"
      isActive={selectedStyle.en == item.en}
      onClick={() => {
        setSelectedStyle(item);
      }}
    >
      {item.name}
    </Button>
  ));

  const onSubmitHandler = () => {
    const genre = selectedGenre.reduce((acc: any[], cur: any) => {
      if (cur.isActive) {
        acc.push(cur.name);
      }
      return acc;
    }, []);
    const characters = actorState.map((item) => ({
      name: item.name,
      characterPrompt: item.characterPrompt,
    }));
    onGenerateMutation.mutate({
      title,
      promptKor,
      style: selectedStyle.en,
      generateType: "SIMPLE",
      genre,
      wishCutCnt: 5,
      characters,
    });
  };
  return (
    <MainSection className={styles["story-simple-section"]}>
      <div className={styles["story-simple-sec-title"]}>
        작품을 만들어볼까요?
      </div>
      <p className={styles["story-simple-sec-dscr"]}>
        당신만의 스토리를 채워보세요.
      </p>
      <div className={styles["story-simple-sec-main"]}>
        <Input
          type="input"
          label="제목"
          placeholder="작품의 제목을 입력해주세요."
          onChange={(e: any) => {
            setTitle(e.target.value);
          }}
          value={title}
        />
        <article
          className={clsx(styles["story-simple-sec-item"], styles["style"])}
        >
          <div className={styles["item-title"]}>
            <span>스타일</span>
            <div className={styles["line"]} />
          </div>
          <div className={styles["item-btns"]}>{renderStyle}</div>
        </article>
        <article
          className={clsx(styles["story-simple-sec-item"], styles["style"])}
        >
          <div className={styles["item-title"]}>
            <span>
              장르<span>(중복가능)</span>
            </span>
            <div className={styles["line"]} />
          </div>
          <div className={styles["item-btns"]}>{renderGenre}</div>
        </article>
        <article
          className={clsx(styles["story-simple-sec-item"], styles["actor"])}
        >
          <div className={styles["item-title"]}>
            <span>등장인물</span>
            <div className={styles["line"]} />
          </div>
          <div className={styles["item-main"]}>
            {renderActors}
            <div className={styles["actor-btn-section"]}>
              <button onClick={onAddActor} className={styles["actor-add-btn"]}>
                <FiPlusCircle />
                <span>등장인물을 더 추가해보세요!</span>
              </button>
            </div>
          </div>
        </article>
        <article
          className={clsx(styles["story-simple-sec-item"], styles["style"])}
        >
          <div className={styles["item-title"]}>
            <span>스토리</span>
            <div className={styles["line"]} />
          </div>
          <div className={styles["item-main"]}>
            <Input
              type="textarea"
              label="스토리"
              height="618px"
              placeholder="가급적이면 자세히 묘사해주세요."
              value={promptKor}
              onChange={(e: any) => {
                setPromptKor(e.target.value);
              }}
            />
          </div>
        </article>
        <Button
          onClick={onSubmitHandler}
          isLoading={onGenerateMutation.isPending}
        >
          작품 생성
        </Button>
      </div>
    </MainSection>
  );
}
