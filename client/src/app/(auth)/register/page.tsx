"use client";

import Button from "@/components/common/Button";
import styles from "./register.module.scss";
import ModalContainer from "@/components/common/ModalContainer";
import Input from "@/components/common/Input";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export default function Page() {
  const requestUrl = `${process.env.NEXT_PUBLIC_SERVER_URL}/users/signup`;
  const router = useRouter();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
    passwordconfig: "",
    nickname: "",
  });

  const queryClient = useQueryClient();
  const registerMutation = useMutation({
    mutationFn: async (data: any) => {
      const response = await fetch(requestUrl, {
        method: "POST",
        body: JSON.stringify(data),
        headers: { "Content-Type": "application/json" },
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      return response.json();
    },
    onSuccess: (data, variables, context) => {
      //   queryClient.invalidateQueries({ queryKey: ["archives-my"] });
      router.push("/login");
    },
  });
  const handleInputChange = (e) => {
    const { name, value } = e.target;

    setFormData((prevFormData) => ({
      ...prevFormData,
      [name]: value,
    }));
  };
  const onSubmitHandler = () => {
    registerMutation.mutate({
      email: formData.email,
      password: formData.password,
      passwordconfig: formData.passwordconfig,
      nickname: formData.nickname,
    });
  };
  return (
    <section className={styles.section}>
      <ModalContainer>
        <div className={styles["section-inner"]}>
          <div className={styles["section-inner-title"]}>
            가입 정보를 입력해주세요!
          </div>
          <div className={styles["section-inner-inputs"]}>
            <Input
              placeholder="이메일"
              label="이메일"
              name="email"
              onChange={handleInputChange}
              value={formData.email}
            />
            <Input
              placeholder="비밀번호"
              label="비밀번호"
              name="password"
              onChange={handleInputChange}
              value={formData.password}
              InputType="password"
            />
            <Input
              placeholder="비밀번호 확인"
              label="비밀번호 확인"
              name="passwordconfig"
              onChange={handleInputChange}
              value={formData.passwordconfig}
              InputType="password"
            />
            <Input
              placeholder="닉네임"
              label="닉네임"
              name="nickname"
              onChange={handleInputChange}
              value={formData.nickname}
            />
          </div>

          <Button
            type="normal"
            color="primary"
            className={styles["register-btn"]}
            onClick={onSubmitHandler}
          >
            회원가입
          </Button>
        </div>
      </ModalContainer>
    </section>
  );
}
