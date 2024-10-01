import styles from "./generate-link.module.scss";
import Link from "next/link";

import { FaBook } from "react-icons/fa6";
import { AiOutlineOpenAI } from "react-icons/ai";

type Props = {
  type?: string;
};
export default function GenerateLink({ type = "simple" }: Props) {
  return type == "simple" ? (
    <Link href="/storys/simple" className={styles["btn-container"]}>
      <div className={styles["btn-container-box"]}>
        <div>
          <AiOutlineOpenAI />
        </div>
      </div>
      <div className={styles["btn-title"]}>간편 생성 타입</div>
      <p className={styles["btn-dscr"]}>
        아트 스타일, 작품 분위기, 캐릭터 설명등을 입력하고 내용을 입력하면 AI가
        자동으로 만화를 생성합니다.
      </p>
    </Link>
  ) : (
    <Link href="/advance" className={styles["btn-container"]}>
      <div className={styles["btn-container-box"]}>
        <div>
          <FaBook />
        </div>
      </div>
      <div className={styles["btn-title"]}>
        세부 생성 타입<span>(출시 예정)</span>
      </div>
      <p className={styles["btn-dscr"]}>
        아트 스타일, 작품 분위기, 캐릭터 설명등을 입력하고 내용을 한 컷씩 직접
        설명하여 만화를 생성합니다.
      </p>
    </Link>
  );
}
