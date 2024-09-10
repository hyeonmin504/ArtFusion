import MainSection from "@/components/common/MainSection";
import styles from "./archives.module.scss";
import RatioContainer from "@/components/common/RatioContainer";
import ArchiveContainer from "@/components/archives/ArchiveContainer";

export default function Page() {
  return (
    <MainSection className={styles["archive-section"]}>
      <div className={styles["archive-sec-title"]}>아카이브</div>
      <p className={styles["archive-sec-dscr"]}>
        사람들이 올린 다양한 작품을 확인해보세요.
      </p>
      <div className={styles["archive-sec-main"]}>
        <ArchiveContainer imgSrc="/exam3.jpeg" title="해골 병사" />
        <ArchiveContainer imgSrc="/exam4.jpeg" title="연애 혁명" />
        <ArchiveContainer imgSrc="/exam5.jpeg" title="초인 시대" />
        <ArchiveContainer imgSrc="/toon_exam.jpeg" title="별품소" />
        <ArchiveContainer imgSrc="/exam3.jpeg" />
        <ArchiveContainer />
      </div>
    </MainSection>
  );
}
