package com.example.FileCRUD.controller;

import com.example.FileCRUD.entity.Attachment;
import com.example.FileCRUD.payload.ApiResponce;
import com.example.FileCRUD.payload.AttachmentDto;
import com.example.FileCRUD.payload.FilterDto;
import com.example.FileCRUD.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/file")
public class AttachmentController {
    @Autowired
    AttachmentService attachmentService;

    /**
     * Bu fayl yuklash uchun yozilgan method.
     * Request ni Service class ga yo'naltirib yuboradi va DB ga saqlanadi
     *
     * @param request da file bo'lib keladi
     * @return ApiResponce ( message, status, object (saveFileId) ) qaytaradi
     * @throws IOException xatolik qaytaradi
     */
    @PostMapping("/upload")
    public HttpEntity<?> uploadFile(MultipartHttpServletRequest request) throws IOException {
        ApiResponce apiResponce = attachmentService.upload(request);
        return ResponseEntity.status(apiResponce.isStatus() ? 201 : 409).body(apiResponce);
    }

    /**
     * fayllarni DB dan olish uchun yozilgan method.
     *
     * @param: orderBy – sort(ASC, DESC) qilish uchun parametr bo'lib kelsadi
     * @return: Fayl nomi, turi, o'lchami va yaratilgan sanasi List ko'rinishida qaytariladi
     */
    @CrossOrigin(origins = "http://localhost:8080")
    @GetMapping
    public HttpEntity<?> getAllFile(@RequestParam String orderBy) {
        List<AttachmentDto> allFile = attachmentService.getAllFile(orderBy);
        return ResponseEntity.ok(allFile);
    }

    /**
     * Faylni yuklab beruvchi servicega yo'naltiradi
     *
     * @param id       bo'yicha fayl yuklanadi
     * @param response da faylni serverdan yuklab response bilan qaytaradi
     * @return ApiResponce(message, status) qaytaradi
     * @throws IOException xatolik
     */
    @GetMapping("/download/{id}")
    public HttpEntity<?> downloadById(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        ApiResponce apiResponce = attachmentService.downloadFileById(id, response);
        return ResponseEntity.status(apiResponce.isStatus() ? 200 : 409).body(apiResponce);
    }

    /**
     * Faylni update qiluvchi servicega yo'natiradi
     *
     * @param id  bo'yicha update qiladi
     * @param dto da fayl nomi keladi
     * @return ApiResponse(message, status) qaytaradi
     */
    @PutMapping("/{id}")
    public HttpEntity<?> editFile(@PathVariable Integer id, @RequestBody AttachmentDto dto) {
        ApiResponce apiResponce = attachmentService.editFile(id, dto);
        return ResponseEntity.status(apiResponce.isStatus() ? 202 : 409).body(apiResponce);
    }

    /**
     * Faylni o'chirish uchun yozilgan servicega yo'naltiruvchi method
     *
     * @param id bo'yicha o'chiradi
     * @return ApiResponse(message, status) qaytaradi
     */
    @DeleteMapping("/{id}")
    public HttpEntity<?> deleteFile(@PathVariable Integer id) {
        ApiResponce apiResponce = attachmentService.deleteFile(id);
        return ResponseEntity.status(apiResponce.isStatus() ? 204 : 409).body(apiResponce);
    }

    /**
     * Faylni filter qilib beruvchi servicega yubororuvchi method
     *
     * @param filterDto filter malumotlari keladi
     * @return da filter bo'lgan malumotlar qaytariladi
     */
    @PostMapping("/filter")
    public HttpEntity<?> sortFile(@RequestBody FilterDto filterDto) {
        List<Attachment> attachmentList = attachmentService.filterFile(filterDto);
        return ResponseEntity.ok(attachmentList);
    }
}
