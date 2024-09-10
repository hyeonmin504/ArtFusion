import RatioContainer from "@/components/common/RatioContainer";
import styles from "./archive-container.module.scss";
import clsx from "clsx";

type Props = {
  imgSrc?: string;
  title?: string;
  description?: string;
  profile?: string;
  tags?: string[];
  className?: string;
};
export default function ArchiveContainer({
  className,
  imgSrc,
  title = "타이틀",
  description = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
  profile = "글쟁이",
  tags = ["판타지", "무협"],
}: Props) {
  const renderTags = tags.map((item, idx) => (
    <div className={styles.tag} key={title + item + idx}>
      {item}
    </div>
  ));
  return (
    <article className={clsx(styles.container, className)}>
      <RatioContainer imgSrc={imgSrc} />
      <div className={styles.contents}>
        <div className={styles.title}>{title}</div>
        <p className={styles.description}>{description}</p>
      </div>
      <div className={styles.etc}>
        <div className={styles.profile}>{profile}</div>
        <div className={styles.tags}>{renderTags}</div>
      </div>
    </article>
  );
}
