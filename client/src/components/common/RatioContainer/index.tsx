import Image from "next/image";
import styles from "./ratio-container.module.scss";
import img from "/public/exam.png";
type Props = {
  imgSrc?: string;
  isImage?: boolean;
  children?: React.ReactNode;
};

export default function RatioContainer({
  imgSrc,
  isImage = true,
  children,
}: Props) {
  return (
    <div className={styles["ratio-container"]}>
      {isImage && <Image src={imgSrc || img} alt="arcive image" fill={true} />}
      {!isImage && children}
    </div>
  );
}
