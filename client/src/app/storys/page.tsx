import MainSection from "@/components/common/MainSection";
import styles from "./storys.module.scss";
import GenerateLink from "@/components/storys/GenerateLink";

export default function Page() {
  return (
    <MainSection className={styles["story-section"]}>
      <div className={styles["story-sec-title"]}>작품을 만들어볼까요?</div>
      <p className={styles["story-sec-dscr"]}>먼저 타입을 선택해주세요.</p>
      <div className={styles["story-sec-btns"]}>
        <GenerateLink type="simple" />
        <GenerateLink type="advance" />
      </div>
    </MainSection>
  );
}
