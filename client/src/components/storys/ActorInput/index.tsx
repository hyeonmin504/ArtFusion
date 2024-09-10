import Input from "@/components/common/Input";
import styles from "./actor-input.module.scss";

type Props = {
  itemId?: number;
  onChange?: any;
  nameValue?: string;
  dscrValue?: string;
  className?: string;
};
export default function ActorInput({
  itemId,
  onChange,
  nameValue,
  dscrValue,
  className,
}: Props) {
  return (
    <div className={styles.container}>
      <Input
        label="이름"
        placeholder="이름"
        className={styles.actor}
        name="name"
        onChange={onChange}
        value={nameValue}
        itemId={itemId}
        isMulti
      />
      <Input
        type="textarea"
        name="characterPrompt"
        label="설명"
        height="280px"
        placeholder="가급적이면 등장인물의 배경이나 생김새를 자세하게 묘사해주세요."
        className={styles.dscr}
        onChange={onChange}
        value={dscrValue}
        itemId={itemId}
        isMulti
      />
    </div>
  );
}
