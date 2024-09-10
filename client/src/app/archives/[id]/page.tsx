"use client";

import MainSection from "@/components/common/MainSection";
import styles from "./archive-id.module.scss";
import CutImage from "@/components/temporarys/CutImage";
import Button from "@/components/common/Button";
import ModalContainer from "@/components/common/ModalContainer";
import { useState } from "react";
import Input from "@/components/common/Input";
export default function Page() {
  const [modalActive, setModalActive] = useState(false);

  const onClickModalHandler = () => {
    setModalActive(!modalActive);
  };
  return (
    <MainSection className={styles["arc-section"]}>
      <div className={styles["arc-section-inner"]}>
        <div className={styles["arc-sec-title"]}>별을 품은 소드마스터</div>
        <p className={styles["arc-sec-dscr"]}>블라드의 이야기.</p>
        <div className={styles["arc-sec-main"]}>
          <CutImage type="read" "/>
          <CutImage type="read" />
          <CutImage type="read" />
          <CutImage type="read" />
          <CutImage type="read" />
          <CutImage type="read" />
        </div>
      </div>
    </MainSection>
  );
}
