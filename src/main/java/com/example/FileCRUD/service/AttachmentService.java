package com.example.FileCRUD.service;

import com.example.FileCRUD.entity.Attachment;
import com.example.FileCRUD.entity.AttachmentContent;
import com.example.FileCRUD.payload.ApiResponce;
import com.example.FileCRUD.payload.AttachmentDto;
import com.example.FileCRUD.payload.FilterDto;
import com.example.FileCRUD.repository.AttachmentContentRepository;
import com.example.FileCRUD.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class AttachmentService {
    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    AttachmentContentRepository attachmentContentRepository;

    /**
     * Bu fayl yuklash uchun yozilgan service.
     * Request ni  qabul qilib, uni Attachment va AttachmentContent ga bo'ladi va DB da ikkita jadvalda saqlanadi
     *
     * @param request da file bo'lib keladi
     * @return ApiResponce ( message, status, object (saveFileId) ) qaytaradi
     * @throws IOException xatolik qaytaradi
     */
    public ApiResponce upload(MultipartHttpServletRequest request) throws IOException {

        // Fayl formatini tekshirish (pdf, image, xls)
        Iterator<String> fileNames = request.getFileNames();
        MultipartFile file = request.getFile(fileNames.next());
        assert file != null;
        String contentType = file.getContentType();
        assert contentType != null;
        if (!contentType.equals("application/pdf") && !contentType.equals("application/vnd.ms-excel") && !contentType.equals("image/jpeg")) {
            return new ApiResponce("Fayl formati to'g'ri kelmadi", false);
        }

        //Fayl hajmini tekshirish
        if (file.getSize() >= 5 * 1024 * 1024)
            return new ApiResponce("Fayl hajmi 5-mb dan kichik bolishi kerak", false);

        // Fayl avvaldan mavjudligini tekshirish
        Optional<Attachment> exists = attachmentRepository.findByNameAndSizeAndContextType(file.getOriginalFilename(), file.getSize(), file.getContentType());
        if (exists.isPresent())
            return new ApiResponce("Bu fayl allaqachon yuklangan", false);

        // Fayl malumotlarini yuklash
        Attachment attachment = new Attachment();
        attachment.setName(file.getOriginalFilename());
        attachment.setSize(file.getSize());
        attachment.setContextType(file.getContentType());
        Attachment saveAttachment = attachmentRepository.save(attachment);

        // Fayl baytlarini yuklash
        AttachmentContent attachmentContent = new AttachmentContent();
        attachmentContent.setBytes(file.getBytes());
        attachmentContent.setAttachment(saveAttachment);
        attachmentContentRepository.save(attachmentContent);
        return new ApiResponce("Fayl yuklandi", true, saveAttachment.getId());
    }

    /**
     * DB dan fayl malumotlarini olib beruchi method.
     * Bunda faylning turi aniqlanib qaytariladi (Faylning contentType emas)
     *
     * @param orderBy sort qilish uchun parametr bo'lib kelsadi
     * @return Fayl id, nomi, turi, o'lchami va yaratilgan sanasi List ko'rinishida qaytariladi
     */
    public List<AttachmentDto> getAllFile(String orderBy) {
        List<Attachment> attachmentList;
        List<AttachmentDto> attachmentDtoList = new ArrayList<>();
        if (orderBy.equals("DESC"))
            attachmentList = attachmentRepository.findAll(Sort.by(Sort.Direction.DESC, "name"));
        else {
            attachmentList = attachmentRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        }

        //  Fayl formatini to'g'irlash
        String name;
        String type;
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        for (Attachment attachment : attachmentList) {

            int index = attachment.getName().lastIndexOf(".");
            name = attachment.getName().substring(0, index);
            type = attachment.getName().substring(index);

            attachmentDtoList.add(
                    new AttachmentDto(attachment.getId(), name, type, attachment.getSize(), dateFormat.format(attachment.getCreatedAt()))
            );
        }
        return attachmentDtoList;
    }

    /**
     * Faylni yuklab oluvchi method
     *
     * @param id       bo'yicha yuklaydi
     * @param response da faylni serverdan yuklab response bilan qaytaradi
     * @return ApiResponce(message, status) qaytaradi
     * @throws IOException xatolik
     */
    public ApiResponce downloadFileById(Integer id, HttpServletResponse response) throws IOException {

        // Check attachment exists
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isEmpty())
            return new ApiResponce("Fayl topilmadi", false);

        // Check attachmentContent exists
        Optional<AttachmentContent> optionalAttachmentContent = attachmentContentRepository.findByAttachmentId(optionalAttachment.get().getId());
        if (optionalAttachmentContent.isEmpty())
            return new ApiResponce("Fayl contenti topilmadi", false);

        // Download attachment
        Attachment attachment = optionalAttachment.get();
        response.setHeader("Content-Disposition", "attachment; filename=" + attachment.getName());
        response.setContentType(attachment.getContextType());
        FileCopyUtils.copy(optionalAttachmentContent.get().getBytes(), response.getOutputStream());
        return new ApiResponce("Fayl yuklandi", true);
    }

    /**
     * Fayl malumotlarini id bo'yicha update qiladi
     *
     * @param id  bo'yicha topib oladi
     * @param dto dagi fayl nomiga almashtiriladi
     * @return ApiResponce(message, status) qaytaradi
     */
    public ApiResponce editFile(Integer id, AttachmentDto dto) {

        // Fayl mavjudligini tekshirish
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isEmpty())
            return new ApiResponce("Fayl topilmadi", false);

        // Faylni o'zgartirish
        Attachment attachment = optionalAttachment.get();
        int index = attachment.getName().lastIndexOf(".");
        attachment.setName(dto.getName() + attachment.getName().substring(index));

        Attachment save = attachmentRepository.save(attachment);
        return new ApiResponce("Fayl o'zgartirildi", true);
    }

    /**
     * Faylni o'chirish uchun yozilgan method
     *
     * @param id bo'yicha o'chiradi
     * @return ApiResponse(message, status) qaytaradi
     */
    public ApiResponce deleteFile(Integer id) {
        // Cascade type dan foydalanmadim!!!
        try {
            Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
            if (optionalAttachment.isPresent()) {
                Optional<AttachmentContent> optionalAttachmentContent = attachmentContentRepository.findByAttachmentId(optionalAttachment.get().getId());
                optionalAttachmentContent.ifPresent(attachmentContent -> attachmentContentRepository.deleteById(attachmentContent.getId()));
            }
            attachmentRepository.deleteById(id);
            return new ApiResponce("Fayl o'chirildi", true);
        } catch (Exception e) {
            return new ApiResponce("Fayl o'chirilmadi, xatolik bor", false);
        }
    }

    /**
     * Faylni filter qilib beruvchi method.
     *
     * @param filterDto da frontenddan malumot keladi
     * @return Filterlangan fayllarni qaytaradi
     */
    public List<Attachment> filterFile(FilterDto filterDto) {

        //  Name bo'yicha filter qilindi
        List<Attachment> attachmentList = filterDto.getSearchByName().isEmpty() ?
                attachmentRepository.findAll() :
                attachmentRepository.findAllByNameContaining(filterDto.getSearchByName());

        // Type bo'yicha filter qilindi
        int index;
        String type;
        if (!filterDto.getType().isEmpty()) {
            for (int i = 0; i < attachmentList.size(); i++) {
                index = attachmentList.get(i).getName().lastIndexOf(".");
                type = attachmentList.get(i).getName().substring(index + 1);
                if (!type.equals(filterDto.getType())) {
                    attachmentList.remove(i);
                    i--;
                }
            }
        }

        // Size bo'yicha filter qilindi
        List<Attachment> newAttachmentList = new ArrayList<>();
        for (Attachment attachment : attachmentList) {
            if (filterDto.isSize1() && attachment.getSize() < 1024 * 1024) {
                newAttachmentList.add(attachment);
            }
            if (filterDto.isSize2())
                if (1024 * 1024 < attachment.getSize() && attachment.getSize() < 2 * 1024 * 1024) {
                    newAttachmentList.add(attachment);
                }
            if (filterDto.isSize3() && attachment.getSize() > 3 * 1024 * 1024) {
                newAttachmentList.add(attachment);
            }
        }
        if (filterDto.isSize1() || filterDto.isSize2() || filterDto.isSize3()) {
            attachmentList = newAttachmentList;
        }

        // Ikkita vaqt oralig'idagilarni filter qilindi
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        if (!filterDto.getDateFrom().isEmpty()) {
            LocalDateTime dateFrom;
            for (int i = 0; i < attachmentList.size(); i++) {
                dateFrom = LocalDateTime.parse(filterDto.getDateFrom(), formatter);
                boolean after = attachmentList.get(i).getCreatedAt().toLocalDateTime().isAfter(dateFrom);
                if (!after) {
                    attachmentList.remove(i);
                    i--;
                }
            }
        }
        if (!filterDto.getDateUntil().isEmpty()) {
            LocalDateTime dateUntil;
            for (int i = 0; i < attachmentList.size(); i++) {
                dateUntil = LocalDateTime.parse(filterDto.getDateUntil(), formatter);
                boolean before = attachmentList.get(i).getCreatedAt().toLocalDateTime().isBefore(dateUntil);
                if (!before) {
                    attachmentList.remove(i);
                    i--;
                }
            }
        }

        return attachmentList;
    }
}
