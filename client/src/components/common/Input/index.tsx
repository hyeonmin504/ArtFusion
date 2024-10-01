"use client";
import clsx from "clsx";
import { useState } from "react";
import styles from "./input.module.scss";

type Props = {
  label?: string;
  placeholder?: string;
  wrong?: boolean;
  wrongText?: string;
  onChange?: any;
  onSubmit?: () => void;
  isLoading?: boolean;
  value?: string;
  className?: string;
  height?: string;
  type?: string;
  name?: string;
  InputType?: string;
  itemId?: number;
  isMulti?: boolean;
};

export default function Input({
  className,
  label = "label",
  wrong,
  wrongText = "값을 입력해주세요.",
  onChange,
  name,
  placeholder = "입력해주세요.",
  isLoading,
  value,
  height,
  type = "input",
  InputType = "text",
  isMulti = false,
  itemId,
}: Props) {
  const [isFocus, setIsFocus] = useState(false);
  const [chatText, setChatText] = useState("");
  //여러개의 중첩 객체 input을 관리할 때 사용
  const onMultiChange = (e) => {
    onChange && onChange(e, itemId);
  };
  return (
    <div
      className={clsx(styles.container, styles[type], className, {
        [styles.active]: isFocus,
        [styles.wrong]: wrong,
      })}
    >
      <p className={clsx(styles.label)}>{label}</p>
      {type == "input" ? (
        <input
          type={InputType}
          className={styles.input}
          onFocus={() => setIsFocus(true)}
          onBlur={() => setIsFocus(false)}
          {...(placeholder && { placeholder })}
          {...(isLoading && { disabled: true })}
          {...(name && { name })}
          value={value || chatText}
          onChange={isMulti ? onMultiChange : onChange}
        />
      ) : (
        <textarea
          className={styles.textarea}
          onFocus={() => setIsFocus(true)}
          onBlur={() => setIsFocus(false)}
          style={{ height }}
          {...(placeholder && { placeholder })}
          {...(isLoading && { disabled: true })}
          {...(name && { name })}
          // value={text || chatText}
          onChange={isMulti ? onMultiChange : onChange}
          // onKeyUp={onKeyDownText}
        />
      )}
      <p className={styles["sub-label"]}>{wrong && !isFocus && wrongText}</p>
    </div>
  );
}
