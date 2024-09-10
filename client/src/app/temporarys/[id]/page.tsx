"use client";

import MainSection from "@/components/common/MainSection";
import styles from "./tempoaray.module.scss";
import CutImage from "@/components/temporarys/CutImage";
import Button from "@/components/common/Button";
import ModalContainer from "@/components/common/ModalContainer";
import { useState } from "react";
import Input from "@/components/common/Input";
import { useQuery } from "@tanstack/react-query";
export default function Page({ params }: { params: { id: string } }) {
  const requestUrl = `${process.env.NEXT_PUBLIC_SERVER_URL}/story/temporary/${params.id}`;
  const [modalActive, setModalActive] = useState(false);

  //GET
  const { isPending, data, isError, refetch, error } = useQuery({
    queryKey: ["archives-my"],
    queryFn: async () => {
      const response = await fetch(requestUrl, { credentials: "include" });

      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      return response.json();
    },
    retry: false,
  });
  console.log(data?.data);
  const onClickModalHandler = () => {
    setModalActive(!modalActive);
  };
  const renderImages =
    data &&
    data?.data?.sceneFormatForms.map((item: any) => (
      <CutImage
        modalClickHandler={onClickModalHandler}
        imageUrl={item.imageUrl}
        bg={item.background}
        dscr={item.description}
        dialogue={item.dialogue}
        key={item.imageUrl}
      />
    ));

  return (
    <MainSection className={styles["temp-section"]}>
      <div className={styles["temp-section-inner"]}>
        <div className={styles["temp-sec-title"]}>이제 마무리해볼까요?</div>
        <p className={styles["temp-sec-dscr"]}>수정할 부분을 확인해주세요.</p>
        <div className={styles["temp-sec-main"]}>
          {renderImages}
          <Button>작품 생성</Button>
        </div>
      </div>
      {modalActive && (
        <div className={styles["modal-wrapper"]} onClick={onClickModalHandler}>
          <ModalContainer
            type="absolute"
            onClick={(e) => e.stopPropagation()}
            className={styles["modal-container"]}
          >
            <div className={styles["modal-title"]}>
              <div className={styles["title"]}>컷1</div>
              <p className={styles["dscr"]}>
                상세하게 입력할 수록 더 높은 퀄리티를 기대할 수 있어요!
              </p>
            </div>
            <div className={styles["modal-main"]}>
              <Input
                label="장면"
                placeholder="장면에 대한 설명"
                type="textarea"
                height="180px"
              />
              <Input
                label="배경"
                placeholder="배경에 대한 설명"
                type="textarea"
                height="120px"
              />
              <Input
                label="대사"
                placeholder="대사에 대한 설명"
                type="textarea"
                height="120px"
              />
            </div>
            <Button>수정하기</Button>
          </ModalContainer>
        </div>
      )}
    </MainSection>
  );
}
