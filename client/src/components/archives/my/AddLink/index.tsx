import Link from "next/link";
import { FaPlus } from "react-icons/fa";
import styles from "./add-link.module.scss";
import RatioContainer from "@/components/common/RatioContainer";

export default function AddLink() {
  return (
    <div className={styles["link-container"]}>
      <Link href="/storys" className={styles["link-container-inner"]}>
        <RatioContainer isImage={false}>
          <div className={styles["box-container"]}>
            <FaPlus />
          </div>
        </RatioContainer>
        <div className={styles["add-text"]}>새로운 작품을 추가해보세요!</div>
      </Link>
    </div>
  );
}
