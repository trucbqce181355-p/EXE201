package com.group1.engagement_service.dto;

public class RedeemRequestDTO {

    private Long reward_id;
    private String type;
    private int points;
    private Long order_id; 

    public Long getReward_id() {
        return reward_id;
    }

    public void setReward_id(Long reward_id) {
        this.reward_id = reward_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Long getOrder_id() {
        return order_id;
    }

    public void setOrder_id(Long order_id) {
        this.order_id = order_id;
    }
}