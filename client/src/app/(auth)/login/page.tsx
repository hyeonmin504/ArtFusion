"use client";

import ModalContainer from "@/components/common/ModalContainer";
import styles from "./login.module.scss";
import Input from "@/components/common/Input";
import Button from "@/components/common/Button";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useState } from "react";

export default function Page() {
  const requestUrl = `${process.env.NEXT_PUBLIC_SERVER_URL}/users/login`;
  const router = useRouter();

  const [id, setId] = useState("");
  const [password, setPassword] = useState("");

  const queryClient = useQueryClient();
  const loginMutation = useMutation({
    mutationFn: async (data: any) => {
      const response = await fetch(requestUrl, {
        method: "POST",
        body: JSON.stringify(data),
        headers: { "Content-Type": "application/json" },
        credentials: "include",
      });

      if (!response.ok) {
        window.alert("정보가 일치하지 않습니다.");
        throw new Error("Network response was not ok");
      }
      return response.json();
    },
    onSuccess: (data, variables, context) => {
      queryClient.invalidateQueries({ queryKey: ["archives-my"] });
      router.push("/archives");
    },
  });

  const onLoginHandler = () => {
    loginMutation.mutate({
      email: id,
      password: password,
    });
  };

  return (
    <section className={styles.section}>
      <ModalContainer>
        <div className={styles["section-inner"]}>
          <div className={styles["section-inner-title"]}>LOGIN</div>
          <div className={styles["section-inner-inputs"]}>
            <Input
              placeholder="아이디를 입력해주세요."
              label="아이디"
              value={id}
              onChange={(e) => {
                setId(e.target.value);
              }}
            />
            <Input
              placeholder="비밀번호를 입력해주세요."
              label="비밀번호"
              InputType="password"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
              }}
            />
          </div>
          <div className={styles["section-inner-btns"]}>
            <Button
              type="normal"
              color="gray"
              className={"login-btn"}
              onClick={onLoginHandler}
            >
              로그인
            </Button>
            <Button
              type="link"
              href="/register"
              color="primary"
              className={styles["register-btn"]}
            >
              회원가입
            </Button>
          </div>
        </div>
      </ModalContainer>
    </section>
  );
}
