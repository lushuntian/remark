package com.sunday.remark.util;

import com.sunday.remark.repository.RemarkRepository;
import com.sunday.remark.repository.entity.Remark;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RemarkRepositoryCounterWrapper extends RemarkRepository {
    private AtomicInteger opsCount = new AtomicInteger(0);

    public RemarkRepositoryCounterWrapper(RemarkRepository remarkRepository) {
        this.remarkRepository = remarkRepository;
    }

    private RemarkRepository remarkRepository;

    @Override
    public List<Remark> listRemarks(String itemId, long curId, int expectCount) {
        opsCount.addAndGet(1);
        return remarkRepository.listRemarks(itemId, curId, expectCount);
    }

    /**
     * 添加评价
     *
     * @param remark 评价对象
     * @return 如果插入成功，返回true，否则返回false
     */
    @Override
    public long addRemark(Remark remark) {
        opsCount.addAndGet(1);
        return remarkRepository.addRemark(remark);
    }

    /**
     * 删除评价
     *
     * @param remark 评价对象
     * @return 如果插入成功，返回true，否则返回false
     */
    @Override
    public boolean removeRemark(Remark remark) {
        opsCount.addAndGet(1);
        return remarkRepository.removeRemark(remark);
    }

    public int getOpsCount(){
        return opsCount.intValue();
    }

    public void resetOpsCount(){
        opsCount.set(0);
    }

}
