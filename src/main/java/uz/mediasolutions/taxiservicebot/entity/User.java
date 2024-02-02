package uz.mediasolutions.taxiservicebot.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import uz.mediasolutions.taxiservicebot.entity.template.AbsUUID;

import javax.persistence.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
@EqualsAndHashCode(callSuper = true)
@Entity(name = "users")
@Where(clause = "deleted=false")
@SQLDelete(sql = "UPDATE users SET deleted=true WHERE id=?")
public class User extends AbsUUID {

    @Column(nullable = false, name = "chat_id")
    private String chatId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "phone_number")
    private String phoneNumber;

}
