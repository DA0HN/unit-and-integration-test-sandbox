package me.gabriel.testingstudy.domain.student;

import me.gabriel.testingstudy.domain.student.exception.EmailAlreadyInUseException;
import me.gabriel.testingstudy.domain.student.exception.StudentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static me.gabriel.testingstudy.domain.student.StudentFactory.makeStudent;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by daohn on 30/05/2021
 * @author daohn
 * @since 30/05/2021
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

  @Mock
  private StudentRepository studentRepository;

  private StudentService sut;
  private static final Long ID_VALID = 1L;
  private static final Long ID_INVALID = -1L;
  private static final Long ID_NULL = null;

  @BeforeEach
  void setUp() {
    this.sut = new StudentServiceImpl(studentRepository);
  }

  @Test
  void whenThereAreRegisteredStudentsThenShouldListAll() {

    sut.findAll();

    verify(studentRepository, times(1)).findAll();
  }

  @Test
  void whenStudentDoesNotHaveRegisteredEmailThenShouldBeCreated() {
    var student = makeStudent();

    when(studentRepository.isEmailAlreadyInUse(anyString())).thenReturn(false);

    sut.create(student);

    var studentArgumentCaptor = ArgumentCaptor.forClass(Student.class);

    verify(studentRepository).save(studentArgumentCaptor.capture());
    verify(studentRepository, times(1)).isEmailAlreadyInUse(anyString());
    verify(studentRepository, times(1)).save(student);

    assertThat(studentArgumentCaptor.getValue()).isEqualTo(student);
  }

  @Test
  void whenStudentHasRegisteredEmailShouldThrowEmailAlreadyInUseException() {

    var student = makeStudent();

    when(studentRepository.isEmailAlreadyInUse(anyString())).thenReturn(true);

    assertThrows(
      EmailAlreadyInUseException.class,
      () -> sut.create(student)
    );

    verify(studentRepository, times(1)).isEmailAlreadyInUse(anyString());
    verify(studentRepository, never()).save(any(Student.class));
  }

  @Test
  void whenGivenIdIsValidThenShouldDeleteStudent() {
    when(studentRepository.existsById(ID_VALID)).thenReturn(true);

    sut.deleteById(ID_VALID);

    verify(studentRepository, times(1)).existsById(ID_VALID);
    verify(studentRepository, times(1)).deleteById(ID_VALID);
  }

  @Test
  void whenGivenIdIsNotValidThenShouldThrowStudentNotFoundException() {

    when(studentRepository.existsById(ID_INVALID)).thenReturn(false);

    var exception = assertThrows(
      StudentNotFoundException.class,
      () -> sut.deleteById(ID_INVALID)
    );

    assertThat(exception).hasMessage("Student with id " + ID_INVALID + " does not exists");
    verify(studentRepository, times(1)).existsById(ID_INVALID);
    verify(studentRepository, never()).deleteById(ID_INVALID);
  }

  @Test
  void whenGivenIdIsNullThenShouldThrowIllegalArgumentException() {
    var exception = assertThrows(
      IllegalArgumentException.class,
      () -> sut.deleteById(ID_NULL)
    );

    assertThat(exception).hasMessage("Id " + ID_NULL + " cannot be null");
    verifyNoInteractions(studentRepository);
  }
}
