package uz.mediasolutions.taxiservicebot.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import uz.mediasolutions.taxiservicebot.entity.template.AbsUUID;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
@EqualsAndHashCode(callSuper = true)
@Entity(name = "cars")
@Where(clause = "deleted=false")
@SQLDelete(sql = "UPDATE cars SET deleted=true WHERE id=?")
public class Car extends AbsUUID {

    @Column(name = "model")
    private String model;

    @Column(name = "number")
    private String number;

}
