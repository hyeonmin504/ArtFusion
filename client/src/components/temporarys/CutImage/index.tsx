"use client";

import Image from "next/image";
import styles from "./cut-image.module.scss";
import examImg from "/public/toon_exam.jpeg";
import { RiRefreshFill } from "react-icons/ri";

type Props = {
  modalClickHandler?: () => void;
  imageUrl?: string;
  bg?: string;
  dscr?: string;
  dialogue?: string;
  type?: string;
};
export default function CutImage({
  modalClickHandler,
  type,
  imageUrl,
  bg,
  dscr,
  dialogue,
}: Props) {
  console.log(imageUrl);

  return (
    <div className={styles["cut-img-container"]}>
      <img src={imageUrl || "/toon_exam.jpeg"} />
      {/* <Image
        src={imageUrl || "/toon_exam.jpeg"}
        alt="img"
        width={100}
        height={100}
      /> */}
      <div className={styles["blur-container"]}>
        <div className={styles["contents"]}>
          {`장면 : ${bg}\n\n내용: ${dscr}\n\n대사: ${dialogue}`}
        </div>
        {type != "read" && (
          <div className={styles["btn-container"]}>
            <button
              className={styles["btn-container-text"]}
              onClick={modalClickHandler}
            >
              이미지 수정
            </button>
            <button
              className={styles["btn-container-text"]}
              onClick={modalClickHandler}
            >
              내용 수정
            </button>
            <button className={styles["btn-container-refresh"]}>
              <RiRefreshFill />
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
