package crichton.domain.services;

import crichton.application.exceptions.CustomException;
import crichton.domain.dtos.ReportDTO;

public interface ReportService {
    ReportDTO.DataResponse transformCsvData(String sourcePath) throws CustomException;
}
