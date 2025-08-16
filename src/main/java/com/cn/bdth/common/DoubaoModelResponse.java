package com.cn.bdth.common;


import java.util.List;


public class DoubaoModelResponse {
    private List<Choice> choices;

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public static class Choice {
        private DoubaoModelRequest.Message message;

        public DoubaoModelRequest.Message getMessage() {
            return message;
        }

        public void setMessage(DoubaoModelRequest.Message message) {
            this.message = message;
        }
    }
}
