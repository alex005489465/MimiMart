package com.mimimart.application.service;

import com.mimimart.domain.member.exception.AddressNotFoundException;
import com.mimimart.infrastructure.persistence.entity.MemberAddress;
import com.mimimart.infrastructure.persistence.repository.MemberAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收貨地址服務
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Service
public class AddressService {

    private final MemberAddressRepository addressRepository;

    public AddressService(MemberAddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    /**
     * 查詢會員的所有地址
     */
    public List<MemberAddress> getAddressList(Long memberId) {
        return addressRepository.findByMemberId(memberId);
    }

    /**
     * 新增地址
     */
    @Transactional
    public MemberAddress createAddress(Long memberId, String recipientName, String phone, String address, Boolean isDefault) {
        // 如果設為預設,先取消其他預設地址
        if (Boolean.TRUE.equals(isDefault)) {
            addressRepository.findByMemberIdAndIsDefaultTrue(memberId)
                    .ifPresent(defaultAddr -> {
                        defaultAddr.setIsDefault(false);
                        addressRepository.save(defaultAddr);
                    });
        }

        MemberAddress memberAddress = new MemberAddress();
        memberAddress.setMemberId(memberId);
        memberAddress.setRecipientName(recipientName);
        memberAddress.setPhone(phone);
        memberAddress.setAddress(address);
        memberAddress.setIsDefault(isDefault != null ? isDefault : false);

        return addressRepository.save(memberAddress);
    }

    /**
     * 更新地址
     */
    @Transactional
    public MemberAddress updateAddress(Long memberId, Long addressId, String recipientName, String phone, String address) {
        MemberAddress memberAddress = addressRepository.findByIdAndMemberId(addressId, memberId)
                .orElseThrow(() -> new AddressNotFoundException("地址不存在"));

        if (recipientName != null) {
            memberAddress.setRecipientName(recipientName);
        }
        if (phone != null) {
            memberAddress.setPhone(phone);
        }
        if (address != null) {
            memberAddress.setAddress(address);
        }

        return addressRepository.save(memberAddress);
    }

    /**
     * 刪除地址
     */
    @Transactional
    public void deleteAddress(Long memberId, Long addressId) {
        MemberAddress memberAddress = addressRepository.findByIdAndMemberId(addressId, memberId)
                .orElseThrow(() -> new AddressNotFoundException("地址不存在"));

        addressRepository.delete(memberAddress);
    }

    /**
     * 設為預設地址
     */
    @Transactional
    public void setDefaultAddress(Long memberId, Long addressId) {
        // 取消其他預設地址
        addressRepository.findByMemberIdAndIsDefaultTrue(memberId)
                .ifPresent(defaultAddr -> {
                    defaultAddr.setIsDefault(false);
                    addressRepository.save(defaultAddr);
                });

        // 設定新的預設地址
        MemberAddress memberAddress = addressRepository.findByIdAndMemberId(addressId, memberId)
                .orElseThrow(() -> new AddressNotFoundException("地址不存在"));

        memberAddress.setIsDefault(true);
        addressRepository.save(memberAddress);
    }
}
